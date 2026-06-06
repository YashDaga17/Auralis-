package com.brandforge.app.domain.lead

import com.brandforge.app.core.ai.gemini.GeminiGenerationApi
import com.brandforge.app.core.ai.gemini.GeminiGenerationConfig
import com.brandforge.app.core.ai.gemini.GeminiGenerationContent
import com.brandforge.app.core.ai.gemini.GeminiGenerationPart
import com.brandforge.app.core.ai.gemini.GeminiGenerationRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javax.inject.Inject

class LeadDetectionAgent @Inject constructor(
    private val geminiGenerationApi: GeminiGenerationApi,
    private val leadRepository: LeadRepository,
    private val gson: Gson,
) {
    suspend fun classify(input: LeadInteractionInput): Lead {
        require(input.creatorId.isNotBlank()) { "creatorId is required" }
        require(input.text.isNotBlank()) { "Interaction text is required" }

        val rawResponse = generateClassification(input)
        val payload = parsePayload(rawResponse)
        val classifiedAt = System.currentTimeMillis()
        return leadRepository.persist(
            LeadDetectionInput(
                id = "lead-${input.creatorId}-${input.receivedAt}-$classifiedAt",
                creatorId = input.creatorId.trim(),
                sourceType = input.sourceType,
                platform = input.platform.trim().ifBlank { "Unknown" },
                authorHandle = input.authorHandle.trim().ifBlank { "Unknown" },
                text = input.text.trim(),
                classification = payload.classification,
                confidence = payload.confidence,
                suggestedReply = payload.suggestedReply,
                priority = payload.priority,
                reason = payload.reason,
                receivedAt = input.receivedAt,
                classifiedAt = classifiedAt,
                rawModelResponse = rawResponse,
            ),
        )
    }

    private suspend fun generateClassification(input: LeadInteractionInput): String =
        geminiGenerationApi.generateContent(
            model = GeminiFlashLiteModel,
            request = GeminiGenerationRequest(
                contents = listOf(
                    GeminiGenerationContent(
                        parts = listOf(
                            GeminiGenerationPart(text = buildPrompt(input)),
                        ),
                    ),
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.18,
                    maxOutputTokens = 700,
                ),
            ),
        ).candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.joinToString(separator = "\n") { it.text.orEmpty() }
            .orEmpty()
            .trim()
            .ifBlank { error("LeadDetectionAgent received an empty Gemini response") }

    private fun buildPrompt(input: LeadInteractionInput): String =
        """
        You are BrandForge Lead Detection Agent.
        Classify one creator audience interaction for engagement value and urgency.
        Use Gemini Flash Lite style: fast, precise, conservative.

        Allowed classification values:
        - Lead
        - Question
        - Collaboration
        - Feedback
        - PR Risk
        - Ignore

        Allowed priority values:
        - Critical
        - High
        - Medium
        - Low

        Classification rules:
        - Lead: purchase intent, service inquiry, booking intent, pricing, availability, or business fit.
        - Question: genuine audience question that deserves a useful answer.
        - Collaboration: partnership, sponsorship, guesting, cross-promotion, or brand deal intent.
        - Feedback: useful praise, criticism, requested improvement, testimonial signal, or product insight.
        - PR Risk: complaint, legal threat, safety concern, accusation, misinformation risk, reputational escalation, or public anger.
        - Ignore: spam, bots, low-signal emoji-only, harassment without useful action, or irrelevant chatter.

        Do not create external CRM tasks.
        Do not trigger WhatsApp flows.
        Do not create PR workflows.
        Return only valid JSON with this exact shape:
        {
          "classification": "Lead | Question | Collaboration | Feedback | PR Risk | Ignore",
          "confidence": 0.0,
          "suggestedReply": "short creator-safe reply",
          "priority": "Critical | High | Medium | Low",
          "reason": "specific reason for the classification"
        }

        CREATOR ID
        ${input.creatorId.trim()}

        SOURCE TYPE
        ${input.sourceType.label}

        PLATFORM
        ${input.platform.trim().ifBlank { "Unknown" }}

        AUTHOR
        ${input.authorHandle.trim().ifBlank { "Unknown" }}

        INTERACTION TEXT
        ${input.text.trim()}
        """.trimIndent()

    private fun parsePayload(rawResponse: String): ParsedLeadClassification {
        val json = extractJsonObject(rawResponse)
        val payload = gson.fromJson(json, LeadClassificationJson::class.java)
        return ParsedLeadClassification(
            classification = payload.classification.toClassification(),
            confidence = payload.confidence.coerceIn(0f, 1f),
            suggestedReply = payload.suggestedReply.trim(),
            priority = payload.priority.toPriority(),
            reason = payload.reason.trim().ifBlank {
                error("LeadDetectionAgent response is missing a reason")
            },
        )
    }

    private fun extractJsonObject(rawResponse: String): JsonObject {
        val trimmed = rawResponse
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        require(start >= 0 && end > start) {
            "LeadDetectionAgent response did not contain a JSON object"
        }
        return JsonParser.parseString(trimmed.substring(start, end + 1)).asJsonObject
    }

    private fun String?.toClassification(): LeadClassification {
        val normalized = normalizeEnumToken()
        return when (normalized) {
            "LEAD" -> LeadClassification.Lead
            "QUESTION" -> LeadClassification.Question
            "COLLABORATION", "COLLAB" -> LeadClassification.Collaboration
            "FEEDBACK" -> LeadClassification.Feedback
            "PRRISK", "PUBLICRELATIONSRISK", "REPUTATIONRISK" -> LeadClassification.PRRisk
            "IGNORE" -> LeadClassification.Ignore
            else -> error("Unknown lead classification: $this")
        }
    }

    private fun String?.toPriority(): LeadPriority {
        val normalized = normalizeEnumToken()
        return when (normalized) {
            "CRITICAL" -> LeadPriority.Critical
            "HIGH" -> LeadPriority.High
            "MEDIUM" -> LeadPriority.Medium
            "LOW" -> LeadPriority.Low
            else -> error("Unknown lead priority: $this")
        }
    }

    private fun String?.normalizeEnumToken(): String =
        orEmpty()
            .trim()
            .uppercase()
            .filter { it.isLetterOrDigit() }

    private data class LeadClassificationJson(
        val classification: String = "",
        val confidence: Float = 0f,
        val suggestedReply: String = "",
        val priority: String = "",
        val reason: String = "",
    )

    private data class ParsedLeadClassification(
        val classification: LeadClassification,
        val confidence: Float,
        val suggestedReply: String,
        val priority: LeadPriority,
        val reason: String,
    )

    private companion object {
        const val GeminiFlashLiteModel = "models/gemini-2.5-flash-lite"
    }
}

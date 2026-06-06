package com.brandforge.app.domain.competitor

import com.brandforge.app.core.ai.gemini.GeminiGenerationApi
import com.brandforge.app.core.ai.gemini.GeminiGenerationConfig
import com.brandforge.app.core.ai.gemini.GeminiGenerationContent
import com.brandforge.app.core.ai.gemini.GeminiGenerationPart
import com.brandforge.app.core.ai.gemini.GeminiGenerationRequest
import com.brandforge.app.domain.memory.BrandDna
import com.brandforge.app.domain.memory.MemoryShard
import com.brandforge.app.domain.trend.TrendOpportunity
import com.google.gson.Gson
import com.google.gson.JsonParser
import javax.inject.Inject

class GapAnalysisEngine @Inject constructor(
    private val geminiGenerationApi: GeminiGenerationApi,
    private val gson: Gson,
) {
    suspend fun analyze(
        competitor: Competitor,
        content: List<CompetitorContent>,
        brandDna: BrandDna,
        memories: List<MemoryShard>,
        existingOpportunities: List<TrendOpportunity>,
    ): List<CompetitorGapCandidate> {
        require(content.isNotEmpty()) { "Competitor content is required for gap analysis" }
        val rawResponse = generateAnalysis(
            competitor = competitor,
            content = content,
            brandDna = brandDna,
            memories = memories,
            existingOpportunities = existingOpportunities,
        )
        return parseCandidates(rawResponse)
            .filter { it.gap.isNotBlank() && it.recommendation.isNotBlank() }
            .take(MaxInsights)
            .ifEmpty { error("GapAnalysisEngine produced no usable competitor insights") }
    }

    private suspend fun generateAnalysis(
        competitor: Competitor,
        content: List<CompetitorContent>,
        brandDna: BrandDna,
        memories: List<MemoryShard>,
        existingOpportunities: List<TrendOpportunity>,
    ): String =
        geminiGenerationApi.generateContent(
            model = GeminiModel,
            request = GeminiGenerationRequest(
                contents = listOf(
                    GeminiGenerationContent(
                        parts = listOf(GeminiGenerationPart(text = buildPrompt(competitor, content, brandDna, memories, existingOpportunities))),
                    ),
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.32,
                    maxOutputTokens = 1_800,
                ),
            ),
        ).candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.joinToString(separator = "\n") { it.text.orEmpty() }
            .orEmpty()
            .trim()
            .ifBlank { error("GapAnalysisEngine received an empty Gemini response") }

    private fun buildPrompt(
        competitor: Competitor,
        content: List<CompetitorContent>,
        brandDna: BrandDna,
        memories: List<MemoryShard>,
        existingOpportunities: List<TrendOpportunity>,
    ): String =
        """
        You are BrandForge Competitor Intelligence Agent.
        Analyze competitor activity to produce content gaps and strategic opportunities for the creator's Digital Twin.
        Do not produce a generic competitor dashboard. Produce actionable gaps that can influence Trend Opportunities, Content Generation, and Twin Chat.

        Answer these questions through the JSON output:
        - What are competitors posting?
        - What content themes dominate?
        - What themes are missing?
        - What opportunities exist?
        - What should the creator do differently?

        Return only valid JSON with this exact shape:
        {
          "insights": [
            {
              "pattern": "dominant competitor focus",
              "frequency": "how often it appears in supplied content",
              "gap": "specific underserved theme",
              "recommendation": "what the creator should do",
              "confidence": 0.0,
              "opportunityScore": 0.0,
              "reasoning": "why this gap is strategically valid",
              "recommendedContentFormat": "Reel Script | Instagram Carousel | X Thread",
              "recommendedHook": "creator-ready hook",
              "recommendedAngle": "differentiated content angle"
            }
          ]
        }

        COMPETITOR
        Name: ${competitor.name}
        Platform: ${competitor.platform.label}
        URL: ${competitor.url}

        CREATOR BRAND DNA
        Name: ${brandDna.creatorName}
        Archetype: ${brandDna.archetype}
        Voice Rules: ${brandDna.voiceRulesJson}
        Creator Goals: ${brandDna.businessGoalsJson}
        Banned Claims: ${brandDna.bannedClaimsJson}

        COMPETITOR CONTENT
        ${content.joinToString(separator = "\n") { item ->
            "- [${item.id}] ${item.title}; summary=${item.summary}; source=${item.sourceUrl}; engagement=${item.engagementEstimate}"
        }}

        CREATOR MEMORY
        ${memories.joinToString(separator = "\n") { memory ->
            "- [${memory.id}] ${memory.type.name}: ${memory.title}; ${memory.summary}"
        }.ifBlank { "- none available" }}

        EXISTING TREND OPPORTUNITIES
        ${existingOpportunities.joinToString(separator = "\n") { opportunity ->
            "- [${opportunity.id}] ${opportunity.title}; score=${(opportunity.opportunityScore * 100).toInt()}%; format=${opportunity.recommendedFormat}; rationale=${opportunity.rationale}"
        }.ifBlank { "- none available" }}

        DECISION RULES
        - Prefer gaps that fit Brand DNA and creator goals.
        - Penalize gaps already covered by existing Trend Opportunities.
        - Avoid copying competitor positioning.
        - Make recommended hooks concrete and mobile-native.
        - Set confidence based only on supplied evidence.
        """.trimIndent()

    private fun parseCandidates(rawResponse: String): List<CompetitorGapCandidate> {
        val objectText = rawResponse.extractJsonObject()
        val root = JsonParser.parseString(objectText).asJsonObject
        val insights = root.getAsJsonArray("insights") ?: error("GapAnalysisEngine response missing insights array")
        return insights.map { element ->
            val item = gson.fromJson(element, CompetitorGapJson::class.java)
            CompetitorGapCandidate(
                pattern = item.pattern.trim(),
                frequency = item.frequency.trim(),
                gap = item.gap.trim(),
                recommendation = item.recommendation.trim(),
                confidence = item.confidence.coerceIn(0f, 1f),
                reasoning = item.reasoning.trim(),
                recommendedContentFormat = item.recommendedContentFormat.trim(),
                recommendedHook = item.recommendedHook.trim(),
                recommendedAngle = item.recommendedAngle.trim(),
                opportunityScore = item.opportunityScore.coerceIn(0f, 1f),
            )
        }
    }

    private fun String.extractJsonObject(): String {
        val trimmed = removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        require(start >= 0 && end > start) {
            "GapAnalysisEngine response did not contain a JSON object"
        }
        return trimmed.substring(start, end + 1)
    }

    private data class CompetitorGapJson(
        val pattern: String = "",
        val frequency: String = "",
        val gap: String = "",
        val recommendation: String = "",
        val confidence: Float = 0f,
        val opportunityScore: Float = 0f,
        val reasoning: String = "",
        val recommendedContentFormat: String = "",
        val recommendedHook: String = "",
        val recommendedAngle: String = "",
    )

    private companion object {
        const val GeminiModel = "models/gemini-2.5-flash"
        const val MaxInsights = 5
    }
}

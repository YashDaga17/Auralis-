package com.brandforge.app.domain.twin

import com.brandforge.app.core.ai.gemini.GeminiGenerationApi
import com.brandforge.app.core.ai.gemini.GeminiGenerationConfig
import com.brandforge.app.core.ai.gemini.GeminiGenerationContent
import com.brandforge.app.core.ai.gemini.GeminiGenerationPart
import com.brandforge.app.core.ai.gemini.GeminiGenerationRequest
import javax.inject.Inject

class TwinChatAgent @Inject constructor(
    private val contextAssembler: ContextAssembler,
    private val twinChatRepository: TwinChatRepository,
    private val geminiGenerationApi: GeminiGenerationApi,
) {
    suspend fun sendMessage(creatorId: String, message: String): TwinChatMessage {
        require(creatorId.isNotBlank()) { "creatorId is required" }
        require(message.isNotBlank()) { "message is required" }
        val now = System.currentTimeMillis()
        twinChatRepository.persistMessage(
            TwinChatMessageDraft(
                id = "user-$now",
                creatorId = creatorId,
                role = TwinChatRole.User,
                message = message.trim(),
                createdAt = now,
            ),
        )

        val prompt = contextAssembler.assemble(
            creatorId = creatorId,
            userMessage = message.trim(),
        )
        val response = generateTwinResponse(prompt)
        return twinChatRepository.persistMessage(
            TwinChatMessageDraft(
                id = "twin-${System.currentTimeMillis()}",
                creatorId = creatorId,
                role = TwinChatRole.Twin,
                message = response,
                createdAt = System.currentTimeMillis(),
                memoryIds = prompt.memoryIds,
                trendIds = prompt.trendIds,
                opportunityIds = prompt.opportunityIds,
                contentDraftIds = prompt.contentDraftIds,
            ),
        )
    }

    private suspend fun generateTwinResponse(prompt: TwinPrompt): String =
        geminiGenerationApi.generateContent(
            model = GeminiModel,
            request = GeminiGenerationRequest(
                contents = listOf(
                    GeminiGenerationContent(
                        parts = listOf(GeminiGenerationPart(text = prompt.text)),
                    ),
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.66,
                    maxOutputTokens = 1_500,
                ),
            ),
        ).candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.joinToString(separator = "\n") { it.text.orEmpty() }
            .orEmpty()
            .trim()
            .ifBlank { error("Twin Chat could not generate a Gemini response") }

    private companion object {
        const val GeminiModel = "models/gemini-2.5-flash"
    }
}

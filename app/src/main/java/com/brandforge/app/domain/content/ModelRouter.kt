package com.brandforge.app.domain.content

import com.brandforge.app.core.ai.gemini.GeminiGenerationApi
import com.brandforge.app.core.ai.gemini.GeminiGenerationConfig
import com.brandforge.app.core.ai.gemini.GeminiGenerationContent
import com.brandforge.app.core.ai.gemini.GeminiGenerationPart
import com.brandforge.app.core.ai.gemini.GeminiGenerationRequest
import com.brandforge.app.core.ai.openrouter.OpenRouterChatRequest
import com.brandforge.app.core.ai.openrouter.OpenRouterClient
import com.brandforge.app.core.ai.openrouter.OpenRouterMessage
import javax.inject.Inject

class ModelRouter @Inject constructor(
    private val openRouterClient: OpenRouterClient,
    private val geminiGenerationApi: GeminiGenerationApi,
) {
    suspend fun generate(prompt: PromptPayload): String {
        val openRouterContent = runCatching {
            openRouterClient.createChatCompletion(
                OpenRouterChatRequest(
                    model = OpenRouterModel,
                    messages = listOf(
                        OpenRouterMessage(role = "system", content = prompt.systemPrompt),
                        OpenRouterMessage(role = "user", content = prompt.userPrompt),
                    ),
                    temperature = 0.74,
                    maxTokens = 1_900,
                ),
            ).choices.firstNotNullOfOrNull { it.message?.content }
                .orEmpty()
                .trim()
        }.getOrDefault("")

        if (openRouterContent.isNotBlank()) {
            return openRouterContent
        }

        return geminiGenerationApi.generateContent(
            model = GeminiModel,
            request = GeminiGenerationRequest(
                contents = listOf(
                    GeminiGenerationContent(
                        parts = listOf(
                            GeminiGenerationPart(
                                text = prompt.systemPrompt + "\n\n" + prompt.userPrompt,
                            ),
                        ),
                    ),
                ),
                generationConfig = GeminiGenerationConfig(),
            ),
        ).candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.joinToString(separator = "\n") { it.text.orEmpty() }
            .orEmpty()
            .trim()
            .ifBlank { error("ModelRouter could not generate content from OpenRouter or Gemini") }
    }

    private companion object {
        const val OpenRouterModel = "minimax/minimax-01"
        const val GeminiModel = "models/gemini-2.5-flash"
    }
}

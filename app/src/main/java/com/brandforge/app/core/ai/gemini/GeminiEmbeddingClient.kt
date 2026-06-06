package com.brandforge.app.core.ai.gemini

import com.brandforge.app.core.ai.EmbeddingClient
import com.brandforge.app.domain.memory.EmbeddingVector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiEmbeddingClient @Inject constructor(
    private val api: GeminiEmbeddingApi,
) : EmbeddingClient {
    override suspend fun embed(text: String): EmbeddingVector {
        require(text.isNotBlank()) { "Embedding text cannot be blank" }
        val model = "models/text-embedding-004"
        val response = api.embedContent(
            model = model,
            request = GeminiEmbeddingRequest(
                model = model,
                content = GeminiContent(
                    parts = listOf(GeminiPart(text = text.take(MaxEmbeddingTextLength))),
                ),
            ),
        )
        return EmbeddingVector(
            values = response.embedding?.values.orEmpty(),
        )
    }

    private companion object {
        const val MaxEmbeddingTextLength = 12_000
    }
}

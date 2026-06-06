package com.brandforge.app.core.ai

import com.brandforge.app.domain.memory.EmbeddingVector

interface EmbeddingClient {
    suspend fun embed(text: String): EmbeddingVector
}

package com.brandforge.app.core.ai.gemini

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiEmbeddingApi {
    @POST("v1beta/{model}:embedContent")
    suspend fun embedContent(
        @Path(value = "model", encoded = true) model: String,
        @Body request: GeminiEmbeddingRequest,
    ): GeminiEmbeddingResponse
}

data class GeminiEmbeddingRequest(
    val model: String,
    val content: GeminiContent,
)

data class GeminiContent(
    val parts: List<GeminiPart>,
)

data class GeminiPart(
    val text: String,
)

data class GeminiEmbeddingResponse(
    val embedding: GeminiEmbeddingValues?,
)

data class GeminiEmbeddingValues(
    val values: List<Float> = emptyList(),
)

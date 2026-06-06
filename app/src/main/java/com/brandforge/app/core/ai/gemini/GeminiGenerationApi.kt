package com.brandforge.app.core.ai.gemini

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiGenerationApi {
    @POST("v1beta/{model}:generateContent")
    suspend fun generateContent(
        @Path(value = "model", encoded = true) model: String,
        @Body request: GeminiGenerationRequest,
    ): GeminiGenerationResponse
}

data class GeminiGenerationRequest(
    val contents: List<GeminiGenerationContent>,
    val generationConfig: GeminiGenerationConfig,
)

data class GeminiGenerationContent(
    val role: String = "user",
    val parts: List<GeminiGenerationPart>,
)

data class GeminiGenerationPart(
    @SerializedName("text")
    val text: String? = null,
    @SerializedName(value = "inlineData", alternate = ["inline_data"])
    val inlineData: GeminiInlineData? = null,
)

data class GeminiInlineData(
    @SerializedName(value = "mimeType", alternate = ["mime_type"])
    val mimeType: String,
    @SerializedName("data")
    val data: String,
)

data class GeminiGenerationConfig(
    val temperature: Double = 0.72,
    val maxOutputTokens: Int = 1_800,
    val responseModalities: List<String>? = null,
)

data class GeminiGenerationResponse(
    val candidates: List<GeminiGenerationCandidate> = emptyList(),
)

data class GeminiGenerationCandidate(
    val content: GeminiGenerationContent? = null,
)

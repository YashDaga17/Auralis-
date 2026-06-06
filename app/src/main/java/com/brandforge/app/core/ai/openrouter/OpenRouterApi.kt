package com.brandforge.app.core.ai.openrouter

import retrofit2.http.Body
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: OpenRouterChatRequest,
    ): OpenRouterChatResponse
}

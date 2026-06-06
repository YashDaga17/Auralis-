package com.brandforge.app.core.ai.openrouter

interface OpenRouterClient {
    suspend fun createChatCompletion(request: OpenRouterChatRequest): OpenRouterChatResponse
}

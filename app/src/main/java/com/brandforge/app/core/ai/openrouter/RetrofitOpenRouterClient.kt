package com.brandforge.app.core.ai.openrouter

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitOpenRouterClient @Inject constructor(
    private val api: OpenRouterApi,
) : OpenRouterClient {
    override suspend fun createChatCompletion(request: OpenRouterChatRequest): OpenRouterChatResponse =
        api.createChatCompletion(request)
}

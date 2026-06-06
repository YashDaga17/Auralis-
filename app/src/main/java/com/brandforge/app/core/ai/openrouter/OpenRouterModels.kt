package com.brandforge.app.core.ai.openrouter

import com.google.gson.annotations.SerializedName

data class OpenRouterChatRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val temperature: Double? = null,
    @SerializedName("max_tokens")
    val maxTokens: Int? = null,
)

data class OpenRouterMessage(
    val role: String,
    val content: String,
)

data class OpenRouterChatResponse(
    val id: String?,
    val model: String?,
    val choices: List<OpenRouterChoice> = emptyList(),
    val usage: OpenRouterUsage? = null,
)

data class OpenRouterChoice(
    val index: Int?,
    val message: OpenRouterMessage?,
    @SerializedName("finish_reason")
    val finishReason: String?,
)

data class OpenRouterUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int?,
    @SerializedName("completion_tokens")
    val completionTokens: Int?,
    @SerializedName("total_tokens")
    val totalTokens: Int?,
)

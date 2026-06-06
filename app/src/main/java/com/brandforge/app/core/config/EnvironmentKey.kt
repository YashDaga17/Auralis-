package com.brandforge.app.core.config

enum class EnvironmentKey(
    val buildConfigName: String,
    val requiredForProduction: Boolean = true,
) {
    OpenRouterApiKey("OPENROUTER_API_KEY"),
    GeminiApiKey("GEMINI_API_KEY"),
    FirecrawlApiKey("FIRECRAWL_API_KEY"),
    ApifyApiToken("APIFY_API_TOKEN", requiredForProduction = false),
    QdrantUrl("QDRANT_URL"),
    QdrantApiKey("QDRANT_API_KEY"),
    YouTubeApiKey("YOUTUBE_API_KEY"),
}

enum class ApiEndpoint {
    OpenRouter,
    Gemini,
    Firecrawl,
    Apify,
    Qdrant,
    YouTube,
}

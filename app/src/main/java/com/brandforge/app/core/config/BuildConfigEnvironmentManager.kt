package com.brandforge.app.core.config

import com.brandforge.app.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildConfigEnvironmentManager @Inject constructor(
    private val secretManager: SecretManager,
) : EnvironmentManager {
    private val secrets: Map<EnvironmentKey, String>
        get() = mapOf(
            EnvironmentKey.OpenRouterApiKey to BuildConfig.OPENROUTER_API_KEY,
            EnvironmentKey.GeminiApiKey to BuildConfig.GEMINI_API_KEY,
            EnvironmentKey.FirecrawlApiKey to BuildConfig.FIRECRAWL_API_KEY,
            EnvironmentKey.ApifyApiToken to BuildConfig.APIFY_API_TOKEN,
            EnvironmentKey.QdrantUrl to BuildConfig.QDRANT_URL,
            EnvironmentKey.QdrantApiKey to BuildConfig.QDRANT_API_KEY,
            EnvironmentKey.YouTubeApiKey to BuildConfig.YOUTUBE_API_KEY,
        )

    private val endpoints: Map<ApiEndpoint, String>
        get() = mapOf(
            ApiEndpoint.OpenRouter to BuildConfig.OPENROUTER_BASE_URL,
            ApiEndpoint.Gemini to BuildConfig.GEMINI_BASE_URL,
            ApiEndpoint.Firecrawl to BuildConfig.FIRECRAWL_BASE_URL,
            ApiEndpoint.Apify to BuildConfig.APIFY_BASE_URL,
            ApiEndpoint.Qdrant to BuildConfig.QDRANT_URL.ifBlank { "https://localhost/" },
            ApiEndpoint.YouTube to BuildConfig.YOUTUBE_BASE_URL,
        )

    override fun secret(key: EnvironmentKey): String = secrets[key].orEmpty()

    override fun endpointBaseUrl(endpoint: ApiEndpoint): String =
        endpoints[endpoint].orEmpty().ensureTrailingSlash()

    override fun validate(): EnvironmentValidation {
        val secretStatus = EnvironmentKey.entries.map { key ->
            val value = secret(key)
            EnvironmentSecret(
                key = key,
                configured = secretManager.isConfigured(value),
                redactedValue = secretManager.redact(value),
            )
        }
        return EnvironmentValidation(
            secrets = secretStatus,
            missingRequiredKeys = secretStatus
                .filter { !it.configured && it.key.requiredForProduction }
                .map { it.key },
            endpointBaseUrls = ApiEndpoint.entries.associateWith(::endpointBaseUrl),
        )
    }

    private fun String.ensureTrailingSlash(): String =
        if (endsWith("/")) this else "$this/"
}

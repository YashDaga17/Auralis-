package com.brandforge.app.core.config

data class EnvironmentSecret(
    val key: EnvironmentKey,
    val configured: Boolean,
    val redactedValue: String,
)

data class EnvironmentValidation(
    val secrets: List<EnvironmentSecret>,
    val missingRequiredKeys: List<EnvironmentKey>,
    val endpointBaseUrls: Map<ApiEndpoint, String>,
) {
    val isProductionReady: Boolean = missingRequiredKeys.isEmpty()
    val configuredKeys: List<EnvironmentKey> = secrets
        .filter { it.configured }
        .map { it.key }

    fun warningMessage(): String? =
        if (isProductionReady) {
            null
        } else {
            "Missing environment keys: " +
                missingRequiredKeys.joinToString { it.buildConfigName }
        }
}

interface EnvironmentManager {
    fun secret(key: EnvironmentKey): String
    fun endpointBaseUrl(endpoint: ApiEndpoint): String
    fun validate(): EnvironmentValidation
}

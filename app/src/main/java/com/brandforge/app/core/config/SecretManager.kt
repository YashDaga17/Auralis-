package com.brandforge.app.core.config

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecretManager @Inject constructor() {
    private val placeholderTokens = setOf(
        "change_me",
        "changeme",
        "todo",
        "replace_me",
        "your_api_key",
        "your_token",
    )

    fun isConfigured(value: String): Boolean {
        val normalized = value.trim()
        if (normalized.isBlank()) return false
        return placeholderTokens.none { token ->
            normalized.equals(token, ignoreCase = true) ||
                normalized.contains(token, ignoreCase = true)
        }
    }

    fun redact(value: String): String {
        val trimmed = value.trim()
        if (!isConfigured(trimmed)) return "(missing)"
        if (trimmed.length <= 8) return "****"
        return trimmed.take(4) + "..." + trimmed.takeLast(4)
    }

    fun bearer(value: String): String = "Bearer $value"
}

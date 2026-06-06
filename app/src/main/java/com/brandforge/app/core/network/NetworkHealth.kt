package com.brandforge.app.core.network

import com.brandforge.app.core.config.EnvironmentValidation

data class NetworkHealth(
    val environmentReady: Boolean,
    val missingKeys: List<String>,
    val endpointCount: Int,
)

fun EnvironmentValidation.toNetworkHealth(): NetworkHealth =
    NetworkHealth(
        environmentReady = isProductionReady,
        missingKeys = missingRequiredKeys.map { it.buildConfigName },
        endpointCount = endpointBaseUrls.size,
    )

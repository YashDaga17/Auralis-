package com.brandforge.app.data.trend

internal fun stableTrendSignalId(prefix: String, sourceUrl: String): String =
    "$prefix-${Integer.toHexString(sourceUrl.hashCode())}"

internal fun String.cleanText(): String =
    replace(Regex("\\s+"), " ").trim()

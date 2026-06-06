package com.brandforge.app.data.competitor

import com.brandforge.app.domain.competitor.CompetitorPlatform
import java.net.URI
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun stableCompetitorId(creatorId: String, url: String): String =
    "competitor-" + stableHash(creatorId.trim().lowercase() + "|" + url.normalizedUrl())

fun stableCompetitorContentId(competitorId: String, sourceUrl: String): String =
    "competitor-content-" + stableHash(competitorId + "|" + sourceUrl.normalizedUrl())

fun stableCompetitorInsightId(competitorId: String, gap: String, createdAt: Long): String =
    "competitor-insight-" + stableHash(competitorId + "|" + gap.cleanCompetitorText() + "|" + createdAt)

fun String.cleanCompetitorText(): String =
    replace(Regex("\\s+"), " ")
        .replace("&amp;", "&")
        .trim()

fun String.normalizedUrl(): String =
    trim()
        .removeSuffix("/")

fun String.detectCompetitorPlatform(): CompetitorPlatform {
    val normalized = lowercase()
    return when {
        "youtube.com" in normalized || "youtu.be" in normalized -> CompetitorPlatform.YouTube
        normalized.startsWith("http://") || normalized.startsWith("https://") -> CompetitorPlatform.Website
        else -> CompetitorPlatform.Unknown
    }
}

fun String.deriveCompetitorName(): String {
    val raw = trim()
    val uri = runCatching { URI(raw) }.getOrNull()
    val pathName = uri?.path
        ?.split("/")
        ?.filter { it.isNotBlank() }
        ?.lastOrNull()
        ?.removePrefix("@")
    val hostName = uri?.host
        ?.removePrefix("www.")
        ?.substringBefore(".")
    return (pathName ?: hostName ?: raw)
        .replace("-", " ")
        .replace("_", " ")
        .cleanCompetitorText()
        .replaceFirstChar { char -> char.titlecase(Locale.US) }
        .ifBlank { "Competitor" }
}

fun String.toCompetitorSearchQuery(name: String): String {
    val uri = runCatching { URI(this) }.getOrNull()
    val host = uri?.host?.removePrefix("www.").orEmpty()
    val path = uri?.path.orEmpty().replace("/", " ")
    return listOf(name, host, path)
        .joinToString(separator = " ")
        .cleanCompetitorText()
        .take(MaxSearchQueryLength)
}

fun String.parseRfc3339Millis(): Long? {
    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
    ).onEach { format ->
        format.timeZone = TimeZone.getTimeZone("UTC")
    }
    return formats.firstNotNullOfOrNull { format ->
        runCatching { format.parse(this)?.time }.getOrNull()
    }
}

private fun stableHash(value: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
    return bytes.take(16).joinToString(separator = "") { "%02x".format(it) }
}

private const val MaxSearchQueryLength = 500

package com.brandforge.app.domain.trend

import com.brandforge.app.domain.memory.BrandDna
import com.brandforge.app.domain.memory.MemoryShard
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class ScoreTrendOpportunityUseCase @Inject constructor() {
    fun score(
        signal: TrendSignal,
        brandDna: BrandDna,
        retrievedMemories: List<MemoryShard>,
        now: Long = System.currentTimeMillis(),
    ): TrendOpportunity {
        val signalTerms = signal.searchableText().tokens()
        val brandTerms = brandDna.brandText().tokens()
        val goalTerms = brandDna.businessGoalsJson.tokens()
        val memoryTerms = retrievedMemories.flatMap { it.searchableText().tokens() }.toSet()

        val goalFit = signalTerms.coverageOf(goalTerms)
        val brandVoiceFit = signalTerms.coverageOf(brandTerms)
        val memoryFit = signalTerms.coverageOf(memoryTerms)
        val brandFitScore = normalized(
            (goalFit * 0.40f) +
                (brandVoiceFit * 0.38f) +
                (memoryFit * 0.22f),
        )
        val freshnessScore = freshnessScore(signal, now)
        val velocityScore = velocityScore(signal, freshnessScore)
        val opportunityScore = normalized(
            (brandFitScore * 0.48f) +
                (velocityScore * 0.30f) +
                (freshnessScore * 0.22f),
        )

        val matchedGoals = signalTerms.intersect(goalTerms).take(4)
        val matchedMemories = signalTerms.intersect(memoryTerms).take(4)
        return TrendOpportunity(
            id = "opp-${signal.id}",
            creatorId = signal.creatorId,
            signalId = signal.id,
            sourceUrl = signal.sourceUrl,
            sourcePlatform = signal.sourcePlatform,
            title = signal.title,
            summary = signal.summary,
            velocityScore = velocityScore,
            freshnessScore = freshnessScore,
            brandFitScore = brandFitScore,
            opportunityScore = opportunityScore,
            recommendedFormat = recommendedFormat(signal),
            rationale = buildRationale(
                signal = signal,
                memoryCount = retrievedMemories.size,
                matchedGoals = matchedGoals,
                matchedMemories = matchedMemories,
                hasPublishedAt = signal.publishedAt != null,
            ),
            createdAt = now,
        )
    }

    private fun freshnessScore(signal: TrendSignal, now: Long): Float {
        val publishedAt = signal.publishedAt ?: return 0.50f
        val ageHours = max(0L, now - publishedAt).toFloat() / MillisPerHour
        return when {
            ageHours <= 6f -> 1f
            ageHours <= 24f -> 0.90f
            ageHours <= 72f -> 0.74f
            ageHours <= 168f -> 0.58f
            else -> 0.30f
        }
    }

    private fun velocityScore(signal: TrendSignal, freshnessScore: Float): Float {
        val rankScore = (1f - ((signal.sourceRank - 1).coerceAtLeast(0) / 10f)).coerceIn(0.15f, 1f)
        val sourceSignal = when {
            signal.sourcePlatform.contains("YouTube", ignoreCase = true) -> 0.92f
            signal.sourcePlatform.contains("Firecrawl", ignoreCase = true) -> 0.76f
            else -> 0.66f
        }
        return normalized((rankScore * 0.58f) + (freshnessScore * 0.24f) + (sourceSignal * 0.18f))
    }

    private fun recommendedFormat(signal: TrendSignal): String {
        val text = signal.searchableText().lowercase()
        return when {
            signal.sourcePlatform.contains("YouTube", ignoreCase = true) -> "YouTube Shorts Script"
            "carousel" in text || "steps" in text || "mistakes" in text || Regex("\\b\\d+\\b").containsMatchIn(text) -> "Instagram Carousel"
            "thread" in text || "x " in text -> "X Thread"
            "whatsapp" in text || "broadcast" in text -> "WhatsApp Broadcast"
            else -> "LinkedIn Post"
        }
    }

    private fun buildRationale(
        signal: TrendSignal,
        memoryCount: Int,
        matchedGoals: List<String>,
        matchedMemories: List<String>,
        hasPublishedAt: Boolean,
    ): String {
        val goalText = matchedGoals.joinToString(", ").ifBlank { "no direct goal keyword match" }
        val memoryText = matchedMemories.joinToString(", ").ifBlank { "no direct memory keyword match" }
        val freshnessText = if (hasPublishedAt) {
            "source publish time available"
        } else {
            "source publish time unavailable"
        }
        return "Rank #${signal.sourceRank} on ${signal.sourcePlatform}; goals: $goalText; memory: $memoryText across $memoryCount shards; $freshnessText."
    }

    private fun BrandDna.brandText(): String =
        listOf(
            creatorName,
            archetype,
            voiceRulesJson,
            bannedClaimsJson,
            businessGoalsJson,
        ).joinToString(separator = " ")

    private fun TrendSignal.searchableText(): String =
        "$title $summary $sourcePlatform"

    private fun MemoryShard.searchableText(): String =
        "$title $summary ${type.name}"

    private fun String.tokens(): Set<String> =
        lowercase()
            .split(Regex("[^a-z0-9]+"))
            .asSequence()
            .map { it.trim() }
            .filter { it.length > 2 }
            .toSet()

    private fun Set<String>.coverageOf(corpus: Set<String>): Float =
        if (isEmpty() || corpus.isEmpty()) {
            0f
        } else {
            (count { it in corpus }.toFloat() / min(size, 12).coerceAtLeast(1)).coerceIn(0f, 1f)
        }

    private fun normalized(value: Float): Float =
        value.coerceIn(0f, 1f)

    private companion object {
        const val MillisPerHour = 60f * 60f * 1000f
    }
}

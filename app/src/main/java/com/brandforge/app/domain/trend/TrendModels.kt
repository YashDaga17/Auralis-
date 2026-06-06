package com.brandforge.app.domain.trend

data class TrendSignalQuery(
    val creatorId: String,
    val query: String,
    val limit: Int = 8,
)

data class TrendSignal(
    val id: String,
    val creatorId: String,
    val sourceUrl: String,
    val sourcePlatform: String,
    val title: String,
    val summary: String,
    val sourceRank: Int,
    val observedAt: Long,
    val publishedAt: Long?,
    val rawPayloadJson: String,
)

data class TrendOpportunity(
    val id: String,
    val creatorId: String,
    val signalId: String,
    val sourceUrl: String,
    val sourcePlatform: String,
    val title: String,
    val summary: String,
    val velocityScore: Float,
    val freshnessScore: Float,
    val brandFitScore: Float,
    val opportunityScore: Float,
    val recommendedFormat: String,
    val rationale: String,
    val createdAt: Long,
)

data class TrendScanResult(
    val signals: List<TrendSignal>,
    val opportunities: List<TrendOpportunity>,
)

package com.brandforge.app.domain.competitor

enum class CompetitorPlatform(
    val label: String,
) {
    YouTube("YouTube"),
    Website("Website"),
    Unknown("Unknown"),
}

data class Competitor(
    val id: String,
    val creatorId: String,
    val name: String,
    val platform: CompetitorPlatform,
    val url: String,
    val lastAnalyzed: Long?,
)

data class CompetitorInput(
    val id: String,
    val creatorId: String,
    val name: String,
    val platform: CompetitorPlatform,
    val url: String,
    val lastAnalyzed: Long?,
)

data class CompetitorContent(
    val id: String,
    val competitorId: String,
    val creatorId: String,
    val title: String,
    val summary: String,
    val publishedAt: Long?,
    val engagementEstimate: String,
    val sourceUrl: String,
    val rawPayloadJson: String,
    val observedAt: Long,
)

data class CompetitorContentInput(
    val id: String,
    val competitorId: String,
    val creatorId: String,
    val title: String,
    val summary: String,
    val publishedAt: Long?,
    val engagementEstimate: String,
    val sourceUrl: String,
    val rawPayloadJson: String,
    val observedAt: Long,
)

data class CompetitorInsight(
    val id: String,
    val competitorId: String,
    val creatorId: String,
    val pattern: String,
    val frequency: String,
    val gap: String,
    val recommendation: String,
    val confidence: Float,
    val reasoning: String,
    val recommendedContentFormat: String,
    val recommendedHook: String,
    val recommendedAngle: String,
    val opportunityScore: Float,
    val createdAt: Long,
)

data class CompetitorInsightInput(
    val id: String,
    val competitorId: String,
    val creatorId: String,
    val pattern: String,
    val frequency: String,
    val gap: String,
    val recommendation: String,
    val confidence: Float,
    val reasoning: String,
    val recommendedContentFormat: String,
    val recommendedHook: String,
    val recommendedAngle: String,
    val opportunityScore: Float,
    val createdAt: Long,
)

data class CompetitorAnalysisRequest(
    val creatorId: String,
    val url: String,
)

data class CompetitorAnalysisResult(
    val competitor: Competitor,
    val content: List<CompetitorContent>,
    val insights: List<CompetitorInsight>,
)

data class CompetitorGapCandidate(
    val pattern: String,
    val frequency: String,
    val gap: String,
    val recommendation: String,
    val confidence: Float,
    val reasoning: String,
    val recommendedContentFormat: String,
    val recommendedHook: String,
    val recommendedAngle: String,
    val opportunityScore: Float,
)

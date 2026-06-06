package com.brandforge.app.domain.competitor

import com.brandforge.app.domain.memory.BrandDna
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryQuery
import com.brandforge.app.domain.trend.TrendOpportunity
import com.brandforge.app.domain.trend.TrendRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class CompetitorAgent @Inject constructor(
    private val competitorRepository: CompetitorRepository,
    private val creatorMemoryRepository: CreatorMemoryRepository,
    private val trendRepository: TrendRepository,
    private val gapAnalysisEngine: GapAnalysisEngine,
) {
    suspend fun analyze(request: CompetitorAnalysisRequest): CompetitorAnalysisResult {
        val creatorId = request.creatorId.trim()
        val url = request.url.trim().removeSuffix("/")
        require(creatorId.isNotBlank()) { "creatorId is required" }
        require(url.isNotBlank()) { "Competitor URL is required" }

        val analyzedAt = System.currentTimeMillis()
        val competitor = competitorRepository.upsertCompetitor(
            (competitorRepository.findCompetitorByUrl(creatorId, url)?.toInput(analyzedAt))
                ?: CompetitorInput(
                    id = competitorId(creatorId, url),
                    creatorId = creatorId,
                    name = url.deriveName(),
                    platform = url.detectPlatform(),
                    url = url,
                    lastAnalyzed = analyzedAt,
                ),
        )

        val brandDna = loadBrandDna(creatorId)
        val fetchedContent = competitorRepository.fetchContent(competitor, limit = FetchLimit)
        val content = competitorRepository.upsertContent(fetchedContent)
        val memories = creatorMemoryRepository.retrieve(
            MemoryQuery(
                creatorId = creatorId,
                query = buildMemoryQuery(competitor, content, brandDna),
                limit = MemoryLimit,
            ),
        )
        val existingOpportunities = withTimeoutOrNull(ContextLoadTimeoutMillis) {
            trendRepository.observeOpportunities(creatorId, limit = OpportunityLimit).first()
        }.orEmpty()
        val candidates = if (content.isEmpty()) {
            listOf(sourceUnavailableCandidate(competitor, brandDna, existingOpportunities))
        } else {
            gapAnalysisEngine.analyze(
                competitor = competitor,
                content = content,
                brandDna = brandDna,
                memories = memories,
                existingOpportunities = existingOpportunities,
            )
        }
        val insights = competitorRepository.upsertInsights(
            candidates.mapIndexed { index, candidate ->
                candidate.toInput(
                    competitor = competitor,
                    createdAt = analyzedAt + index,
                )
            },
        )
        persistCompetitorOpportunities(competitor, insights)
        return CompetitorAnalysisResult(
            competitor = competitor,
            content = content,
            insights = insights,
        )
    }

    private suspend fun loadBrandDna(creatorId: String): BrandDna =
        withTimeoutOrNull(ContextLoadTimeoutMillis) {
            creatorMemoryRepository.observeBrandDna(creatorId).first()
        } ?: error("Brand DNA is required before competitor analysis")

    private fun buildMemoryQuery(
        competitor: Competitor,
        content: List<CompetitorContent>,
        brandDna: BrandDna,
    ): String =
        listOf(
            competitor.name,
            brandDna.archetype,
            brandDna.businessGoalsJson,
            content.joinToString(separator = "\n") { it.title + "\n" + it.summary },
        ).joinToString(separator = "\n")

    private suspend fun persistCompetitorOpportunities(
        competitor: Competitor,
        insights: List<CompetitorInsight>,
    ) {
        val opportunities = insights.map { insight ->
            TrendOpportunity(
                id = "competitor-opportunity-${insight.id}",
                creatorId = insight.creatorId,
                signalId = insight.id,
                sourceUrl = competitor.url,
                sourcePlatform = "Competitor Intelligence / ${competitor.platform.label}",
                title = insight.gap,
                summary = insight.recommendation,
                velocityScore = insight.opportunityScore,
                freshnessScore = 1f,
                brandFitScore = insight.confidence,
                opportunityScore = ((insight.opportunityScore * 0.62f) + (insight.confidence * 0.38f)).coerceIn(0f, 1f),
                recommendedFormat = insight.recommendedContentFormat,
                rationale = "Competitor pattern: ${insight.pattern}. Gap: ${insight.gap}. Reasoning: ${insight.reasoning}. Hook: ${insight.recommendedHook}",
                createdAt = insight.createdAt,
            )
        }
        trendRepository.persistOpportunities(opportunities)
    }

    private fun Competitor.toInput(lastAnalyzed: Long): CompetitorInput =
        CompetitorInput(
            id = id,
            creatorId = creatorId,
            name = name,
            platform = platform,
            url = url,
            lastAnalyzed = lastAnalyzed,
        )

    private fun CompetitorGapCandidate.toInput(
        competitor: Competitor,
        createdAt: Long,
    ): CompetitorInsightInput =
        CompetitorInsightInput(
            id = "competitor-insight-${competitor.id}-$createdAt",
            competitorId = competitor.id,
            creatorId = competitor.creatorId,
            pattern = pattern,
            frequency = frequency,
            gap = gap,
            recommendation = recommendation,
            confidence = confidence,
            reasoning = reasoning,
            recommendedContentFormat = recommendedContentFormat,
            recommendedHook = recommendedHook,
            recommendedAngle = recommendedAngle,
            opportunityScore = opportunityScore,
            createdAt = createdAt,
        )

    private fun sourceUnavailableCandidate(
        competitor: Competitor,
        brandDna: BrandDna,
        existingOpportunities: List<TrendOpportunity>,
    ): CompetitorGapCandidate {
        val existingFocus = existingOpportunities
            .take(3)
            .joinToString { it.title }
            .ifBlank { "no existing trend queue yet" }
        return CompetitorGapCandidate(
            pattern = "Live source unavailable for ${competitor.platform.label}",
            frequency = "0 fetched items from YouTube Data API / Firecrawl",
            gap = "Deep competitor pattern analysis needs a public channel, public video list, or crawlable website page.",
            recommendation = "Keep this competitor saved, then retry with a public channel URL or a competitor content page. Use current Brand DNA (${brandDna.archetype}) and trend queue ($existingFocus) to avoid copying until evidence is available.",
            confidence = 0.32f,
            reasoning = "BrandForge stored the competitor URL, but the configured live sources returned no analyzable content. This is a source-access diagnostic, not a fabricated competitor claim.",
            recommendedContentFormat = "X Thread",
            recommendedHook = "I studied the market, but the real gap starts where competitors stop giving public proof.",
            recommendedAngle = "Build a proof-led post around the creator's own Brand DNA while waiting for richer competitor evidence.",
            opportunityScore = 0.28f,
        )
    }

    private fun competitorId(creatorId: String, url: String): String =
        "competitor-" + Integer.toHexString((creatorId.trim().lowercase() + "|" + url.trim().lowercase()).hashCode())

    private fun String.detectPlatform(): CompetitorPlatform {
        val normalized = lowercase()
        return when {
            "youtube.com" in normalized || "youtu.be" in normalized -> CompetitorPlatform.YouTube
            normalized.startsWith("http://") || normalized.startsWith("https://") -> CompetitorPlatform.Website
            else -> CompetitorPlatform.Unknown
        }
    }

    private fun String.deriveName(): String {
        val raw = substringAfter("://", this)
            .substringBefore("?")
            .removeSuffix("/")
        val candidate = raw
            .split("/")
            .filter { it.isNotBlank() }
            .lastOrNull()
            ?.removePrefix("@")
            ?: raw.substringBefore(".")
        return candidate
            .replace("-", " ")
            .replace("_", " ")
            .trim()
            .replaceFirstChar { it.uppercase() }
            .ifBlank { "Competitor" }
    }

    private companion object {
        const val FetchLimit = 12
        const val MemoryLimit = 8
        const val OpportunityLimit = 10
        const val ContextLoadTimeoutMillis = 2_000L
    }
}

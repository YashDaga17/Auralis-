package com.brandforge.app.domain.content

import com.brandforge.app.domain.competitor.CompetitorRepository
import com.brandforge.app.domain.memory.BrandDna
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryQuery
import com.brandforge.app.domain.memory.MemoryShard
import com.brandforge.app.domain.trend.TrendOpportunity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class ContentAgent @Inject constructor(
    private val creatorMemoryRepository: CreatorMemoryRepository,
    private val contentRepository: ContentRepository,
    private val competitorRepository: CompetitorRepository,
    private val promptAssembler: PromptAssembler,
    private val modelRouter: ModelRouter,
) {
    suspend fun generate(
        trend: TrendOpportunity,
        format: ContentFormat,
    ): ContentDraft {
        val creatorId = trend.creatorId
        val brandDna = loadBrandDna(creatorId)
        val memories = creatorMemoryRepository.retrieve(
            MemoryQuery(
                creatorId = creatorId,
                query = buildMemoryQuery(trend, brandDna, format),
                limit = MemoryLimit,
            ),
        )
        require(memories.isNotEmpty()) {
            "Creator memory is required before content generation"
        }
        val competitorInsights = competitorRepository.latestInsights(creatorId, limit = CompetitorInsightLimit)

        val prompt = promptAssembler.assemble(
            format = format,
            trend = trend,
            brandDna = brandDna,
            memories = memories,
            competitorInsights = competitorInsights,
        )
        val generatedContent = modelRouter.generate(prompt)
        val generatedAt = System.currentTimeMillis()
        return contentRepository.persistDraft(
            ContentDraftInput(
                id = "draft-${trend.id}-${format.name}-$generatedAt",
                creatorId = creatorId,
                title = draftTitle(format, trend),
                content = generatedContent,
                format = format,
                generatedAt = generatedAt,
                sourceTrendId = trend.id,
                opportunityScore = trend.opportunityScore,
                memoryIdsUsed = memories.map { it.id },
                whyGenerated = whyGenerated(
                    trend = trend,
                    format = format,
                    brandDna = brandDna,
                    memories = memories,
                    competitorInsightCount = competitorInsights.size,
                ),
            ),
        )
    }

    private suspend fun loadBrandDna(creatorId: String): BrandDna =
        withTimeoutOrNull(BrandDnaLoadTimeoutMillis) {
            creatorMemoryRepository.observeBrandDna(creatorId).first()
        } ?: error("Brand DNA is required before content generation")

    private fun buildMemoryQuery(
        trend: TrendOpportunity,
        brandDna: BrandDna,
        format: ContentFormat,
    ): String =
        listOf(
            trend.title,
            trend.summary,
            trend.rationale,
            trend.recommendedFormat,
            format.label,
            brandDna.archetype,
            brandDna.businessGoalsJson,
            brandDna.voiceRulesJson,
        ).joinToString(separator = "\n")

    private fun draftTitle(format: ContentFormat, trend: TrendOpportunity): String =
        "${format.label}: ${trend.title}".take(MaxTitleLength)

    private fun whyGenerated(
        trend: TrendOpportunity,
        format: ContentFormat,
        brandDna: BrandDna,
        memories: List<MemoryShard>,
        competitorInsightCount: Int,
    ): String =
        "${format.label} generated because '${trend.title}' scored ${(trend.opportunityScore * 100).toInt()}% as an opportunity for ${brandDna.archetype}; used ${memories.size} creator memory shards and $competitorInsightCount competitor gap insights: ${memories.take(4).joinToString { it.title }}."

    private companion object {
        const val MemoryLimit = 8
        const val CompetitorInsightLimit = 5
        const val BrandDnaLoadTimeoutMillis = 2_000L
        const val MaxTitleLength = 140
    }
}

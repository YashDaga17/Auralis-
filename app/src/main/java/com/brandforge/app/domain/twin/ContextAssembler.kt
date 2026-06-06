package com.brandforge.app.domain.twin

import com.brandforge.app.domain.content.ContentRepository
import com.brandforge.app.domain.competitor.CompetitorInsight
import com.brandforge.app.domain.competitor.CompetitorRepository
import com.brandforge.app.domain.memory.BrandDna
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryQuery
import com.brandforge.app.domain.memory.MemoryShard
import com.brandforge.app.domain.trend.TrendOpportunity
import com.brandforge.app.domain.trend.TrendRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class ContextAssembler @Inject constructor(
    private val creatorMemoryRepository: CreatorMemoryRepository,
    private val trendRepository: TrendRepository,
    private val contentRepository: ContentRepository,
    private val competitorRepository: CompetitorRepository,
    private val twinChatRepository: TwinChatRepository,
) {
    suspend fun assemble(creatorId: String, userMessage: String): TwinPrompt {
        val brandDna = loadBrandDna(creatorId)
        val memories = creatorMemoryRepository.retrieve(
            MemoryQuery(
                creatorId = creatorId,
                query = contextRetrievalQuery(userMessage, brandDna),
                limit = MemoryLimit,
            ),
        )
        val opportunities = withTimeoutOrNull(ContextLoadTimeoutMillis) {
            trendRepository.observeOpportunities(creatorId, limit = OpportunityLimit).first()
        }.orEmpty()
        val contentDrafts = contentRepository.latestDrafts(creatorId, limit = DraftLimit)
        val competitorInsights = competitorRepository.latestInsights(creatorId, limit = CompetitorInsightLimit)
        val recentMessages = twinChatRepository.latestMessages(creatorId, limit = RecentMessageLimit)
        val context = TwinChatContext(
            brandDna = brandDna,
            memories = memories,
            opportunities = opportunities,
            contentDrafts = contentDrafts,
            competitorInsights = competitorInsights,
            recentMessages = recentMessages,
        )
        return TwinPrompt(
            text = buildPrompt(userMessage, context),
            memoryIds = memories.map { it.id },
            trendIds = opportunities.map { it.signalId },
            opportunityIds = opportunities.map { it.id },
            contentDraftIds = contentDrafts.map { it.id },
        )
    }

    private suspend fun loadBrandDna(creatorId: String): BrandDna =
        withTimeoutOrNull(ContextLoadTimeoutMillis) {
            creatorMemoryRepository.observeBrandDna(creatorId).first()
        } ?: error("Brand DNA is required before Twin Chat can answer")

    private fun contextRetrievalQuery(userMessage: String, brandDna: BrandDna): String =
        listOf(
            userMessage,
            brandDna.archetype,
            brandDna.businessGoalsJson,
            brandDna.voiceRulesJson,
        ).joinToString(separator = "\n")

    private fun buildPrompt(userMessage: String, context: TwinChatContext): String =
        """
        You are BrandForge Digital Twin, the creator's autonomous creator strategist.
        You are not ChatGPT. Speak like the creator's AI social media team lead.
        Use only the supplied Brand DNA, creator goals, memory, trend opportunities, competitor gaps, generated content, and recent conversation.
        Be decisive, specific, and mobile-first.
        Do not invent performance numbers, revenue, or personal experiences.
        Every answer must include a short "Citations" section with:
        - Memories: cite memory ids used, or say "none available"
        - Trends: cite trend signal ids or source URLs used, or say "none available"
        - Opportunities: cite opportunity ids used, or say "none available"
        - Competitor Insights: cite competitor insight ids used, or say "none available"
        - Drafts: cite generated draft ids used, or say "none available"

        CREATOR BRAND DNA
        Name: ${context.brandDna.creatorName}
        Archetype: ${context.brandDna.archetype}
        Voice Rules: ${context.brandDna.voiceRulesJson}
        Banned Claims: ${context.brandDna.bannedClaimsJson}
        Creator Goals: ${context.brandDna.businessGoalsJson}

        RELEVANT CREATOR MEMORY
        ${context.memories.memoryBlock()}

        TREND OPPORTUNITIES
        ${context.opportunities.opportunityBlock()}

        COMPETITOR GAPS AND INSIGHTS
        ${context.competitorInsights.competitorInsightBlock()}

        GENERATED CONTENT
        ${context.contentDrafts.joinToString(separator = "\n") { draft ->
            "- [${draft.id}] ${draft.format.label}: ${draft.title}; sourceTrend=${draft.sourceTrendId}; why=${draft.whyGenerated}"
        }.ifBlank { "- none available" }}

        RECENT CONVERSATION
        ${context.recentMessages.joinToString(separator = "\n") { message ->
            "- ${message.role.name}: ${message.message.take(500)}"
        }.ifBlank { "- none available" }}

        USER MESSAGE
        $userMessage

        RESPONSE STYLE
        - If the user asks what to post, recommend one primary move and one backup.
        - If the user asks why content was generated, explain the trend, memories, goals, and draft rationale.
        - If the user asks how the brand is evolving, compare Brand DNA with memory and recent drafts.
        - If the user asks how they differ from competitors or what gap to attack next, use competitor insights directly.
        - Keep the answer practical and creator-specific.
        """.trimIndent()

    private fun List<MemoryShard>.memoryBlock(): String =
        joinToString(separator = "\n") { memory ->
            "- [${memory.id}] ${memory.type.name}: ${memory.title}; ${memory.summary}"
        }.ifBlank { "- none available" }

    private fun List<TrendOpportunity>.opportunityBlock(): String =
        joinToString(separator = "\n") { opportunity ->
            "- [${opportunity.id}] signal=${opportunity.signalId}; ${opportunity.title}; score=${(opportunity.opportunityScore * 100).toInt()}%; format=${opportunity.recommendedFormat}; source=${opportunity.sourceUrl}; rationale=${opportunity.rationale}"
        }.ifBlank { "- none available" }

    private fun List<CompetitorInsight>.competitorInsightBlock(): String =
        joinToString(separator = "\n") { insight ->
            "- [${insight.id}] focus=${insight.pattern}; gap=${insight.gap}; opportunity=${(insight.opportunityScore * 100).toInt()}%; format=${insight.recommendedContentFormat}; hook=${insight.recommendedHook}; angle=${insight.recommendedAngle}; reasoning=${insight.reasoning}"
        }.ifBlank { "- none available" }

    private companion object {
        const val MemoryLimit = 8
        const val OpportunityLimit = 8
        const val DraftLimit = 8
        const val CompetitorInsightLimit = 6
        const val RecentMessageLimit = 10
        const val ContextLoadTimeoutMillis = 2_000L
    }
}

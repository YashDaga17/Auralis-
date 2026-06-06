package com.brandforge.app.core.debug

import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.core.model.MemoryType
import com.brandforge.app.core.network.IoDispatcher
import com.brandforge.app.domain.competitor.CompetitorContentInput
import com.brandforge.app.domain.competitor.CompetitorInput
import com.brandforge.app.domain.competitor.CompetitorInsightInput
import com.brandforge.app.domain.competitor.CompetitorPlatform
import com.brandforge.app.domain.competitor.CompetitorRepository
import com.brandforge.app.domain.content.ContentDraftInput
import com.brandforge.app.domain.content.ContentFormat
import com.brandforge.app.domain.content.ContentRepository
import com.brandforge.app.domain.lead.AudienceInteractionType
import com.brandforge.app.domain.lead.LeadClassification
import com.brandforge.app.domain.lead.LeadDetectionInput
import com.brandforge.app.domain.lead.LeadPriority
import com.brandforge.app.domain.lead.LeadRepository
import com.brandforge.app.domain.memory.BrandDnaInput
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryShardDraft
import com.brandforge.app.domain.trend.TrendOpportunity
import com.brandforge.app.domain.trend.TrendRepository
import com.brandforge.app.domain.trend.TrendSignal
import com.brandforge.app.domain.twin.TwinChatMessageDraft
import com.brandforge.app.domain.twin.TwinChatRepository
import com.brandforge.app.domain.twin.TwinChatRole
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class DebugSeedDataGenerator @Inject constructor(
    private val preferencesStore: CreatorPreferencesStore,
    private val creatorMemoryRepository: CreatorMemoryRepository,
    private val trendRepository: TrendRepository,
    private val contentRepository: ContentRepository,
    private val twinChatRepository: TwinChatRepository,
    private val leadRepository: LeadRepository,
    private val competitorRepository: CompetitorRepository,
    private val errorLogger: GlobalErrorLogger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun seedAll(creatorId: String = DefaultCreatorId): DebugSeedResult =
        withContext(ioDispatcher) {
            runCatching {
                val now = System.currentTimeMillis()
                preferencesStore.setSelectedCreatorId(creatorId)
                preferencesStore.markBrandDnaOnboardingCompleted(true)

                creatorMemoryRepository.upsertBrandDna(
                    BrandDnaInput(
                        creatorId = creatorId,
                        creatorName = "Aarav AI Labs",
                        archetype = "Practical AI educator for Indian founders and creators",
                        voiceRulesJson = """
                            ["Use sharp practical hooks","Explain AI with founder examples","Keep tone confident but useful","Prefer concise frameworks over hype"]
                        """.trimIndent(),
                        bannedClaimsJson = """
                            ["Do not promise guaranteed virality","Do not make income claims","Do not recommend unsafe automation"]
                        """.trimIndent(),
                        businessGoalsJson = """
                            ["Grow inbound consulting leads","Own the AI agents for creators niche","Turn trend windows into daily short-form content"]
                        """.trimIndent(),
                    ),
                )

                val memories = seedMemories(creatorId)
                val signal = TrendSignal(
                    id = "debug-signal-ai-agents-creators",
                    creatorId = creatorId,
                    sourceUrl = "https://example.com/debug/ai-agents-creators",
                    sourcePlatform = "Firecrawl",
                    title = "Creators are adopting AI agents for daily content workflows",
                    summary = "Indian solo creators are looking for agentic tools that monitor trends, draft posts, and surface leads while they sleep.",
                    sourceRank = 1,
                    observedAt = now,
                    publishedAt = now - 2 * 60 * 60 * 1_000,
                    rawPayloadJson = """{"debugSeed":true,"source":"firecrawl"}""",
                )
                trendRepository.persistSignals(listOf(signal))
                val opportunity = TrendOpportunity(
                    id = "debug-opportunity-ai-agents-creators",
                    creatorId = creatorId,
                    signalId = signal.id,
                    sourceUrl = signal.sourceUrl,
                    sourcePlatform = signal.sourcePlatform,
                    title = "AI agents for creator operators",
                    summary = "Position the creator as the practical guide for using AI agents to run content, leads, and trend capture.",
                    velocityScore = 88f,
                    freshnessScore = 91f,
                    brandFitScore = 94f,
                    opportunityScore = 92f,
                    recommendedFormat = ContentFormat.ReelScript.label,
                    rationale = "Matches Brand DNA around practical AI education, current audience curiosity, and lead-generation goals.",
                    createdAt = now,
                )
                trendRepository.persistOpportunities(listOf(opportunity))

                val draft = contentRepository.persistDraft(
                    ContentDraftInput(
                        id = "debug-draft-ai-agents-reel",
                        creatorId = creatorId,
                        title = "AI Agents Are Your Night Shift",
                        content = """
                            HOOK: Everyone talks about AI agents. Nobody shows creators how to put them to work tonight.

                            BEAT 1: Your first agent watches trend windows.
                            BEAT 2: Your second agent checks whether the idea fits your voice.
                            BEAT 3: Your third agent drafts only what supports your business goals.

                            CTA: Comment AGENT if you want the exact creator workflow.
                        """.trimIndent(),
                        format = ContentFormat.ReelScript,
                        generatedAt = now,
                        sourceTrendId = opportunity.id,
                        opportunityScore = opportunity.opportunityScore,
                        memoryIdsUsed = memories.map { it.id },
                        whyGenerated = "Generated because the trend has high freshness, strong Brand DNA fit, and aligns with the creator goal of inbound consulting leads.",
                    ),
                )

                val competitor = competitorRepository.upsertCompetitor(
                    CompetitorInput(
                        id = "debug-competitor-ai-educator",
                        creatorId = creatorId,
                        name = "AI Creator Playbook",
                        platform = CompetitorPlatform.YouTube,
                        url = "https://youtube.com/@debug-ai-creator-playbook",
                        lastAnalyzed = now,
                    ),
                )
                competitorRepository.upsertContent(
                    listOf(
                        CompetitorContentInput(
                            id = "debug-competitor-content-1",
                            competitorId = competitor.id,
                            creatorId = creatorId,
                            title = "Top AI tools for creators",
                            summary = "Broad roundup of AI tools without a mobile-first creator operating workflow.",
                            publishedAt = now - 86_400_000,
                            engagementEstimate = "High comments, medium saves",
                            sourceUrl = competitor.url + "/videos/debug-tools",
                            rawPayloadJson = """{"debugSeed":true}""",
                            observedAt = now,
                        ),
                    ),
                )
                competitorRepository.upsertInsights(
                    listOf(
                        CompetitorInsightInput(
                            id = "debug-competitor-insight-agent-gap",
                            competitorId = competitor.id,
                            creatorId = creatorId,
                            pattern = "Competitors explain AI tools as lists.",
                            frequency = "Weekly",
                            gap = "Few explain autonomous AI agents for daily creator operations.",
                            recommendation = "Own the practical 'AI night shift for creators' angle before tool-roundup channels catch up.",
                            confidence = 0.91f,
                            reasoning = "Competitor content is broad and tool-centric; Brand DNA is stronger at operational frameworks.",
                            recommendedContentFormat = ContentFormat.ReelScript.label,
                            recommendedHook = "Your AI content team should work before you wake up.",
                            recommendedAngle = "Mobile-first autonomous workflow for creators.",
                            opportunityScore = 92f,
                            createdAt = now,
                        ),
                    ),
                )

                val lead = leadRepository.persist(
                    LeadDetectionInput(
                        id = "debug-lead-founder-consulting",
                        creatorId = creatorId,
                        sourceType = AudienceInteractionType.Comment,
                        platform = "Instagram",
                        authorHandle = "@founder_needs_ai",
                        text = "Can you help us set this AI content workflow for our startup page?",
                        classification = LeadClassification.Lead,
                        confidence = 0.93f,
                        suggestedReply = "Yes. DM me your current posting workflow and I will suggest the first agent chain to automate.",
                        priority = LeadPriority.High,
                        reason = "Explicit implementation request from a startup account with high buying intent.",
                        receivedAt = now - 15 * 60 * 1_000,
                        classifiedAt = now,
                        rawModelResponse = """{"debugSeed":true,"classification":"Lead"}""",
                    ),
                )

                val chatMessages = listOf(
                    TwinChatMessageDraft(
                        id = "debug-chat-user-1",
                        creatorId = creatorId,
                        role = TwinChatRole.User,
                        message = "What should I post today?",
                        createdAt = now - 60_000,
                    ),
                    TwinChatMessageDraft(
                        id = "debug-chat-twin-1",
                        creatorId = creatorId,
                        role = TwinChatRole.Twin,
                        message = "Post the AI night-shift reel. It connects your Brand DNA, the AI-agent trend window, and a competitor gap around practical creator workflows.",
                        createdAt = now,
                        memoryIds = memories.map { it.id },
                        trendIds = listOf(signal.id),
                        opportunityIds = listOf(opportunity.id),
                        contentDraftIds = listOf(draft.id),
                    ),
                )
                chatMessages.forEach { twinChatRepository.persistMessage(it) }

                DebugSeedResult(
                    creatorId = creatorId,
                    brandDnaCount = 1,
                    memoryCount = memories.size,
                    trendOpportunityCount = 1,
                    contentDraftCount = 1,
                    competitorCount = 1,
                    leadCount = if (lead.id.isNotBlank()) 1 else 0,
                    twinChatMessageCount = chatMessages.size,
                )
            }.getOrElse { throwable ->
                errorLogger.logBlocking(
                    feature = "Debug Seed",
                    screen = "Debug Panel",
                    throwable = throwable,
                    severity = DebugErrorSeverity.Error,
                )
                throw throwable
            }
        }

    private suspend fun seedMemories(creatorId: String) =
        listOf(
            MemoryShardDraft(
                id = "debug-memory-voice",
                creatorId = creatorId,
                type = MemoryType.AudienceInsight,
                title = "Audience wants practical AI workflows",
                summary = "Best comments come from founders and creators asking for usable AI automations, not generic AI news.",
                sourceUri = "debug://audience/practical-ai",
                retrievalWeight = 0.88f,
            ),
            MemoryShardDraft(
                id = "debug-memory-past-content",
                creatorId = creatorId,
                type = MemoryType.PastContent,
                title = "High performing hook style",
                summary = "Posts that contrast hype with practical operating systems earn more saves and lead comments.",
                sourceUri = "debug://content/high-performing-hooks",
                retrievalWeight = 0.84f,
            ),
            MemoryShardDraft(
                id = "debug-memory-goal",
                creatorId = creatorId,
                type = MemoryType.PerformanceHistory,
                title = "Lead goal: consulting discovery calls",
                summary = "Prioritize content that turns AI curiosity into implementation questions from startup founders.",
                sourceUri = "debug://goals/consulting-leads",
                retrievalWeight = 0.9f,
            ),
        ).map { creatorMemoryRepository.writeMemory(it) }

    private companion object {
        const val DefaultCreatorId = "debug-demo-creator"
    }
}

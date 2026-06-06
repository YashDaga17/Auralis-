package com.brandforge.app.domain.agent

import com.brandforge.app.core.model.AgentLog
import com.brandforge.app.core.model.AgentMode
import com.brandforge.app.core.model.AgentState
import com.brandforge.app.core.model.AgentType
import com.brandforge.app.core.model.BrandForgeState
import com.brandforge.app.core.model.CreatorMetric
import com.brandforge.app.core.model.LeadOpportunity
import com.brandforge.app.core.model.MemoryShard
import com.brandforge.app.core.model.MemoryType
import com.brandforge.app.core.model.OfficeKitMode
import com.brandforge.app.core.model.RiskLevel
import com.brandforge.app.core.model.StrategyBrief
import com.brandforge.app.core.model.TwinChatMessage
import com.brandforge.app.core.model.WorkflowCommand
import com.brandforge.app.domain.metrics.CreatorScoreCalculator
import javax.inject.Inject

class AgentWorkflowEngine @Inject constructor(
    private val scoreCalculator: CreatorScoreCalculator,
) {
    fun reduce(current: BrandForgeState, command: WorkflowCommand, transcript: String? = null): BrandForgeState {
        val logs = listOf(
            buildLog(command, transcript),
            AgentLog(
                id = "log-${command.name}-memory",
                timestamp = "LIVE",
                agent = AgentType.Memory,
                message = "Retrieved Brand DNA, best hooks, competitor moves, and audience objections before acting.",
            ),
        ) + current.logs

        return when (command) {
            WorkflowCommand.GenerateMorningBriefing -> current.copy(
                activeWorkflow = command,
                systemStatus = "Morning briefing assembled from overnight agent runs",
                logs = logs,
                strategyBrief = current.strategyBrief.copy(
                    title = "Today's Creator Twin Briefing",
                    morningBriefing = "Prioritize one authority-building reel, one founder-story LinkedIn post, and one high-intent WhatsApp broadcast for warm leads.",
                    nextBestAction = "Approve the reel script queued by Content Agent, then hand off the competitor teardown to War Room.",
                ),
                agents = current.agents.activate(
                    AgentType.Supervisor to AgentMode.Thinking,
                    AgentType.Overnight to AgentMode.Reviewing,
                    AgentType.Memory to AgentMode.Thinking,
                ),
                chat = current.chat.withTwinReply("I compressed the overnight work into a creator action plan. Run Trend Radar before choosing the next post."),
            )

            WorkflowCommand.ScanTrends -> current.copy(
                activeWorkflow = command,
                systemStatus = "Trend Radar uses Firecrawl and YouTube live sources",
                logs = logs,
                trends = current.trends,
                agents = current.agents.activate(
                    AgentType.Trend to AgentMode.Scanning,
                    AgentType.Virality to AgentMode.Reviewing,
                    AgentType.Competitor to AgentMode.Scanning,
                ),
            )

            WorkflowCommand.GenerateContent -> current.copy(
                activeWorkflow = command,
                systemStatus = "Content Studio generates Reel, Carousel, and X drafts from scored trends",
                logs = logs,
                contentQueue = current.contentQueue,
                agents = current.agents.activate(
                    AgentType.Content to AgentMode.Drafting,
                    AgentType.BrandDna to AgentMode.Reviewing,
                    AgentType.Virality to AgentMode.Reviewing,
                ),
            )

            WorkflowCommand.AnalyzeCompetitors -> current.copy(
                activeWorkflow = command,
                systemStatus = "Competitor Agent building a strategic gap map",
                logs = logs,
                agents = current.agents.activate(
                    AgentType.Competitor to AgentMode.Scanning,
                    AgentType.Supervisor to AgentMode.Thinking,
                ),
                strategyBrief = current.strategyBrief.copy(
                    weeklyMove = "Competitors are over-indexing on tool lists. Your opening is founder operating stories with proof-backed lessons.",
                ),
            )

            WorkflowCommand.DetectLeads -> current.copy(
                activeWorkflow = command,
                systemStatus = "Lead Agent separating curiosity from buying intent",
                logs = logs,
                leads = listOf(
                    LeadOpportunity(
                        id = "lead-cohort-workshop",
                        source = "Instagram comment",
                        intent = "Asked for a founder content workshop for a 12-person cohort",
                        suggestedAction = "Send workshop agenda and ask for preferred week",
                        score = 92,
                    ),
                    LeadOpportunity(
                        id = "lead-youtube-consult",
                        source = "YouTube reply",
                        intent = "Wants help turning long-form videos into weekly Shorts",
                        suggestedAction = "Reply with audit offer and 2 available slots",
                        score = 86,
                    ),
                ) + current.leads.take(4),
                agents = current.agents.activate(
                    AgentType.Lead to AgentMode.Listening,
                    AgentType.SocialListener to AgentMode.Listening,
                    AgentType.Memory to AgentMode.Thinking,
                ),
            )

            WorkflowCommand.AuditPrRisk -> current.copy(
                activeWorkflow = command,
                systemStatus = "PR risk monitor elevated one watch item",
                logs = listOf(
                    AgentLog(
                        id = "log-pr-risk-${current.logs.size}",
                        timestamp = "LIVE",
                        agent = AgentType.SocialListener,
                        message = "One draft uses a claim that needs source verification before publishing.",
                        riskLevel = RiskLevel.Medium,
                    ),
                ) + logs,
                agents = current.agents.activate(
                    AgentType.SocialListener to AgentMode.Escalated,
                    AgentType.BrandDna to AgentMode.Reviewing,
                    AgentType.Supervisor to AgentMode.Reviewing,
                ),
            )

            WorkflowCommand.PrepareWarRoom -> current.copy(
                activeWorkflow = command,
                systemStatus = "Office Kit War Room package staged from phone source of truth",
                logs = logs,
                twin = current.twin.copy(
                    officeKitMode = OfficeKitMode.WarRoom,
                    sourceOfTruth = "iQOO Phone -> Office Kit sync",
                ),
                agents = current.agents.activate(
                    AgentType.Supervisor to AgentMode.HandoffReady,
                    AgentType.Competitor to AgentMode.HandoffReady,
                    AgentType.Content to AgentMode.HandoffReady,
                ),
            )
        }.rebalanceCreatorScore()
    }

    private fun buildLog(command: WorkflowCommand, transcript: String?): AgentLog {
        val agent = when (command) {
            WorkflowCommand.GenerateMorningBriefing -> AgentType.Supervisor
            WorkflowCommand.ScanTrends -> AgentType.Trend
            WorkflowCommand.GenerateContent -> AgentType.Content
            WorkflowCommand.AnalyzeCompetitors -> AgentType.Competitor
            WorkflowCommand.DetectLeads -> AgentType.Lead
            WorkflowCommand.AuditPrRisk -> AgentType.SocialListener
            WorkflowCommand.PrepareWarRoom -> AgentType.Supervisor
        }
        val prefix = transcript?.let { "Voice command \"$it\" routed. " }.orEmpty()
        return AgentLog(
            id = "log-${command.name}-${transcript.orEmpty().hashCode()}",
            timestamp = "LIVE",
            agent = agent,
            message = prefix + "${command.label} workflow started with memory retrieval and brand-fit checks.",
        )
    }

    private fun List<AgentState>.activate(vararg changes: Pair<AgentType, AgentMode>): List<AgentState> {
        val changesByAgent = changes.toMap()
        return map { agent ->
            val mode = changesByAgent[agent.type]
            if (mode == null) {
                agent.copy(progress = (agent.progress * 0.92f).coerceAtLeast(0.08f))
            } else {
                agent.copy(
                    mode = mode,
                    progress = when (mode) {
                        AgentMode.HandoffReady -> 1f
                        AgentMode.Escalated -> 0.82f
                        AgentMode.Reviewing -> 0.76f
                        AgentMode.Drafting -> 0.68f
                        AgentMode.Scanning -> 0.58f
                        AgentMode.Thinking -> 0.63f
                        AgentMode.Listening -> 0.71f
                        AgentMode.Idle -> 0.18f
                    },
                    confidence = (agent.confidence + 3).coerceAtMost(99),
                    currentTask = taskFor(agent.type, mode),
                    lastOutput = outputFor(agent.type, mode),
                )
            }
        }
    }

    private fun taskFor(type: AgentType, mode: AgentMode): String = when (type) {
        AgentType.Supervisor -> "Coordinating cross-agent workflow"
        AgentType.Trend -> "Scoring India-first creator opportunities"
        AgentType.BrandDna -> "Checking tone, vocabulary, proof, and claims"
        AgentType.Content -> "Drafting multi-format content package"
        AgentType.Virality -> "Stress-testing hook and share triggers"
        AgentType.SocialListener -> "Classifying comments, objections, and risk"
        AgentType.Lead -> "Ranking high-intent inbound signals"
        AgentType.Competitor -> "Building competitor gap analysis"
        AgentType.Memory -> "Retrieving creator memory shards"
        AgentType.Overnight -> "Summarizing unattended workflows"
    } + " [$mode]"

    private fun outputFor(type: AgentType, mode: AgentMode): String = when (mode) {
        AgentMode.HandoffReady -> "Office Kit package ready"
        AgentMode.Escalated -> "Needs creator review"
        AgentMode.Drafting -> "Draft set queued"
        AgentMode.Reviewing -> "Quality gate running"
        AgentMode.Scanning -> "Opportunity map refreshed"
        AgentMode.Thinking -> "Strategy synthesis updated"
        AgentMode.Listening -> "Signal queue classified"
        AgentMode.Idle -> "Standing by"
    } + " via ${type.callSign}"

    private fun BrandForgeState.rebalanceCreatorScore(): BrandForgeState {
        val score = scoreCalculator.calculate(creatorMetrics)
        val updated = creatorMetrics.map { metric ->
            if (metric.label == "Creator Score") {
                metric.copy(value = "$score", progress = score / 100f, delta = "+${(score / 14).coerceAtLeast(1)} today")
            } else {
                metric
            }
        }
        return copy(creatorMetrics = updated)
    }

    private fun List<TwinChatMessage>.withTwinReply(message: String): List<TwinChatMessage> =
        listOf(
            TwinChatMessage(
                id = "chat-${size + 1}",
                speaker = "Digital Twin",
                message = message,
                timestamp = "LIVE",
            ),
        ) + take(8)
}

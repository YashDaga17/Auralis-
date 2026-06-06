package com.brandforge.app.data

import com.brandforge.app.core.model.AgentLog
import com.brandforge.app.core.model.AgentMode
import com.brandforge.app.core.model.AgentState
import com.brandforge.app.core.model.AgentType
import com.brandforge.app.core.model.BrandForgeState
import com.brandforge.app.core.model.CompetitorSignal
import com.brandforge.app.core.model.CreatorMetric
import com.brandforge.app.core.model.DigitalTwinSnapshot
import com.brandforge.app.core.model.LeadOpportunity
import com.brandforge.app.core.model.OfficeKitMode
import com.brandforge.app.core.model.RiskLevel
import com.brandforge.app.core.model.StrategyBrief
import com.brandforge.app.core.model.TwinChatMessage

object BrandForgeSeedData {
    fun initialState(): BrandForgeState = BrandForgeState(
        twin = DigitalTwinSnapshot(
            creatorName = "Creator Twin",
            brandArchetype = "Authority Builder",
            maturityLevel = "Unconfigured",
            voiceLockPercent = 0,
            memoryVectors = 0,
            activeAudienceSegments = 0,
            sourceOfTruth = "iQOO Phone",
            lastLearningCycle = "02:40 IST",
            officeKitMode = OfficeKitMode.Companion,
        ),
        creatorMetrics = listOf(
            CreatorMetric("Creator Score", "84", "+6 today", 0.84f),
            CreatorMetric("Brand Consistency", "91%", "+3%", 0.91f),
            CreatorMetric("Growth Score", "78", "+9", 0.78f),
            CreatorMetric("Lead Opportunities", "12", "+4", 0.72f),
            CreatorMetric("Content Output", "18", "+5", 0.68f),
            CreatorMetric("Trend Capture Rate", "64%", "+11%", 0.64f),
            CreatorMetric("Competitor Position", "#2", "closing gap", 0.73f),
            CreatorMetric("Virality Trends", "8.6x", "+1.2x", 0.86f),
        ),
        agents = AgentType.entries.mapIndexed { index, type ->
            AgentState(
                type = type,
                mode = when (type) {
                    AgentType.Supervisor -> AgentMode.Thinking
                    AgentType.Trend -> AgentMode.Scanning
                    AgentType.BrandDna -> AgentMode.Reviewing
                    AgentType.Content -> AgentMode.Drafting
                    AgentType.Virality -> AgentMode.Reviewing
                    AgentType.SocialListener -> AgentMode.Listening
                    AgentType.Lead -> AgentMode.Listening
                    AgentType.Competitor -> AgentMode.Scanning
                    AgentType.Memory -> AgentMode.Thinking
                    AgentType.Overnight -> AgentMode.Idle
                },
                progress = (0.36f + (index * 0.055f)).coerceAtMost(0.92f),
                confidence = 82 + (index % 5) * 3,
                currentTask = type.mission,
                lastOutput = "Memory-aware signal ready",
            )
        },
        logs = listOf(
            AgentLog("log-001", "07:12", AgentType.Overnight, "Overnight workflow waiting for live agent schedules."),
            AgentLog("log-002", "07:14", AgentType.Memory, "Retrieved high-performing founder-story vocabulary before content planning."),
            AgentLog("log-003", "07:16", AgentType.Trend, "Trend Radar is ready for Firecrawl and YouTube scans."),
            AgentLog("log-004", "07:18", AgentType.SocialListener, "Comment cluster shows demand for Hindi-English workshop examples."),
            AgentLog("log-005", "07:21", AgentType.Supervisor, "Creator source of truth locked to phone. Laptop War Room is read-synced only."),
        ),
        trends = emptyList(),
        contentQueue = emptyList(),
        memory = emptyList(),
        competitors = listOf(
            CompetitorSignal("cmp-001", "Creator A", "Posted generic AI tools carousel", "Counter with founder operating workflow", RiskLevel.Low),
            CompetitorSignal("cmp-002", "Coach B", "Workshop waitlist push is live", "Publish proof-led case breakdown today", RiskLevel.Medium),
            CompetitorSignal("cmp-003", "Startup CMO", "LinkedIn thread gaining saves", "Respond with mobile-first creator OS angle", RiskLevel.Low),
        ),
        leads = listOf(
            LeadOpportunity("lead-001", "Instagram DM", "Asked if you consult for founder-led content systems", "Send 3-question qualifier", 88),
            LeadOpportunity("lead-002", "YouTube comment", "Requested a content calendar teardown", "Invite to audit slot", 81),
            LeadOpportunity("lead-003", "LinkedIn reply", "Team wants workshop for sales-led founders", "Share agenda and pricing range", 91),
        ),
        strategyBrief = StrategyBrief(
            title = "Autonomous Creator OS Brief",
            morningBriefing = "Your AI team is waiting for the first live Trend Radar scan.",
            weeklyMove = "Own the lane of mobile-first creator infrastructure, not generic AI tips.",
            prWatch = "Avoid unsupported revenue claims in the workshop broadcast.",
            nextBestAction = "Run Trend Radar, then generate the first creator-specific draft in Content Studio.",
        ),
        chat = listOf(
            TwinChatMessage("chat-001", "Digital Twin", "I kept the phone as source of truth and prepared a laptop handoff without changing canonical memory.", "07:22"),
            TwinChatMessage("chat-002", "Creator", "What should I post first?", "07:23"),
            TwinChatMessage("chat-003", "Digital Twin", "Run Trend Radar first so I can score live opportunities against your memory.", "07:23"),
        ),
        activeWorkflow = null,
        systemStatus = "Autonomous agents online",
    )
}

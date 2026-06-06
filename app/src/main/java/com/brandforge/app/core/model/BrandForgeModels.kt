package com.brandforge.app.core.model

enum class AgentType(
    val displayName: String,
    val callSign: String,
    val mission: String,
) {
    Supervisor("Supervisor Agent", "SUP", "Orchestrates the autonomous creator OS"),
    Trend("Trend Agent", "TRD", "Discovers fast-moving platform opportunities"),
    BrandDna("Brand DNA Agent", "DNA", "Protects creator identity and voice"),
    Content("Content Agent", "CNT", "Produces multi-format social content"),
    Virality("Virality Agent", "VIR", "Scores hooks, shareability, and timing"),
    SocialListener("Social Listener Agent", "LST", "Processes comments and audience signals"),
    Lead("Lead Agent", "LED", "Detects business opportunities"),
    Competitor("Competitor Agent", "CMP", "Tracks competitor moves"),
    Memory("Memory Agent", "MEM", "Retrieves and writes creator memory"),
    Overnight("Overnight Agent", "NGT", "Runs scheduled work while the creator sleeps"),
}

enum class AgentMode {
    Idle,
    Scanning,
    Thinking,
    Drafting,
    Reviewing,
    Listening,
    Escalated,
    HandoffReady,
}

enum class RiskLevel {
    Low,
    Medium,
    High,
}

enum class MemoryType {
    BrandDna,
    PastContent,
    TrendHistory,
    LeadHistory,
    CompetitorObservation,
    AudienceInsight,
    PerformanceHistory,
}

enum class OfficeKitMode {
    Companion,
    WarRoom,
}

enum class WorkflowCommand(
    val label: String,
    val transcriptHint: String,
) {
    GenerateMorningBriefing("Morning briefing", "generate briefing"),
    ScanTrends("Scan trends", "show trends"),
    GenerateContent("Generate content", "generate content"),
    AnalyzeCompetitors("Analyze competitors", "analyze competitors"),
    DetectLeads("Detect leads", "find leads"),
    AuditPrRisk("Audit PR risk", "detect pr risk"),
    PrepareWarRoom("Prepare War Room", "office kit handoff"),
}

data class AgentState(
    val type: AgentType,
    val mode: AgentMode,
    val progress: Float,
    val confidence: Int,
    val currentTask: String,
    val lastOutput: String,
    val riskLevel: RiskLevel = RiskLevel.Low,
)

data class AgentLog(
    val id: String,
    val timestamp: String,
    val agent: AgentType,
    val message: String,
    val riskLevel: RiskLevel = RiskLevel.Low,
)

data class CreatorMetric(
    val label: String,
    val value: String,
    val delta: String,
    val progress: Float,
    val riskLevel: RiskLevel = RiskLevel.Low,
)

data class DigitalTwinSnapshot(
    val creatorName: String,
    val brandArchetype: String,
    val maturityLevel: String,
    val voiceLockPercent: Int,
    val memoryVectors: Int,
    val activeAudienceSegments: Int,
    val sourceOfTruth: String,
    val lastLearningCycle: String,
    val officeKitMode: OfficeKitMode,
)

data class TrendOpportunity(
    val id: String,
    val title: String,
    val platform: String,
    val score: Int,
    val window: String,
    val recommendedFormat: String,
    val rationale: String,
    val riskLevel: RiskLevel = RiskLevel.Low,
)

data class ContentQueueItem(
    val id: String,
    val format: String,
    val title: String,
    val status: String,
    val viralityScore: Int,
    val brandFit: Int,
)

data class MemoryShard(
    val id: String,
    val type: MemoryType,
    val title: String,
    val summary: String,
    val retrievalWeight: Float,
)

data class CompetitorSignal(
    val id: String,
    val competitor: String,
    val signal: String,
    val opportunity: String,
    val urgency: RiskLevel,
)

data class LeadOpportunity(
    val id: String,
    val source: String,
    val intent: String,
    val suggestedAction: String,
    val score: Int,
)

data class StrategyBrief(
    val title: String,
    val morningBriefing: String,
    val weeklyMove: String,
    val prWatch: String,
    val nextBestAction: String,
)

data class TwinChatMessage(
    val id: String,
    val speaker: String,
    val message: String,
    val timestamp: String,
)

data class BrandForgeState(
    val twin: DigitalTwinSnapshot,
    val creatorMetrics: List<CreatorMetric>,
    val agents: List<AgentState>,
    val logs: List<AgentLog>,
    val trends: List<TrendOpportunity>,
    val contentQueue: List<ContentQueueItem>,
    val memory: List<MemoryShard>,
    val competitors: List<CompetitorSignal>,
    val leads: List<LeadOpportunity>,
    val strategyBrief: StrategyBrief,
    val chat: List<TwinChatMessage>,
    val activeWorkflow: WorkflowCommand?,
    val systemStatus: String,
)

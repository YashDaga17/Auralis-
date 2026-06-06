package com.brandforge.app.core.debug

enum class DebugErrorSeverity {
    Warning,
    Error,
    Fatal,
}

data class DebugErrorLog(
    val id: String,
    val timestamp: Long,
    val feature: String,
    val screen: String,
    val message: String,
    val stackTrace: String,
    val severity: DebugErrorSeverity,
)

enum class DebugChecklistStatus(
    val label: String,
) {
    NotTested("NOT TESTED"),
    Pass("PASS"),
    Fail("FAIL"),
}

data class DebugChecklistItem(
    val id: String,
    val label: String,
    val status: DebugChecklistStatus,
    val expectedResult: String,
    val failureConditions: String,
    val criticality: String,
    val updatedAt: Long,
    val notes: String,
)

enum class HealthStatus {
    Pass,
    Warning,
    Fail,
}

data class HealthCheckResult(
    val name: String,
    val status: HealthStatus,
    val reason: String,
)

data class AppHealthSnapshot(
    val checkedAt: Long,
    val results: List<HealthCheckResult>,
) {
    val overallStatus: HealthStatus =
        when {
            results.any { it.status == HealthStatus.Fail } -> HealthStatus.Fail
            results.any { it.status == HealthStatus.Warning } -> HealthStatus.Warning
            else -> HealthStatus.Pass
        }
}

data class DebugSeedResult(
    val creatorId: String,
    val brandDnaCount: Int,
    val memoryCount: Int,
    val trendOpportunityCount: Int,
    val contentDraftCount: Int,
    val competitorCount: Int,
    val leadCount: Int,
    val twinChatMessageCount: Int,
) {
    val summary: String =
        "Seeded $creatorId: DNA=$brandDnaCount, MEM=$memoryCount, TRD=$trendOpportunityCount, " +
            "CNT=$contentDraftCount, CMP=$competitorCount, LED=$leadCount, TWIN=$twinChatMessageCount"
}

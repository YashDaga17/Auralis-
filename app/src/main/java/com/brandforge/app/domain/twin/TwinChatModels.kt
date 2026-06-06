package com.brandforge.app.domain.twin

import com.brandforge.app.domain.content.ContentDraft
import com.brandforge.app.domain.competitor.CompetitorInsight
import com.brandforge.app.domain.memory.BrandDna
import com.brandforge.app.domain.memory.MemoryShard
import com.brandforge.app.domain.trend.TrendOpportunity

enum class TwinChatRole {
    User,
    Twin,
}

data class TwinChatMessage(
    val id: String,
    val creatorId: String,
    val role: TwinChatRole,
    val message: String,
    val createdAt: Long,
    val memoryIds: List<String>,
    val trendIds: List<String>,
    val opportunityIds: List<String>,
    val contentDraftIds: List<String>,
)

data class TwinChatMessageDraft(
    val id: String,
    val creatorId: String,
    val role: TwinChatRole,
    val message: String,
    val createdAt: Long,
    val memoryIds: List<String> = emptyList(),
    val trendIds: List<String> = emptyList(),
    val opportunityIds: List<String> = emptyList(),
    val contentDraftIds: List<String> = emptyList(),
)

data class TwinChatContext(
    val brandDna: BrandDna,
    val memories: List<MemoryShard>,
    val opportunities: List<TrendOpportunity>,
    val contentDrafts: List<ContentDraft>,
    val competitorInsights: List<CompetitorInsight>,
    val recentMessages: List<TwinChatMessage>,
)

data class TwinPrompt(
    val text: String,
    val memoryIds: List<String>,
    val trendIds: List<String>,
    val opportunityIds: List<String>,
    val contentDraftIds: List<String>,
)

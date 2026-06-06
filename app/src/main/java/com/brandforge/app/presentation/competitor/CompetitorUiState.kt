package com.brandforge.app.presentation.competitor

import com.brandforge.app.domain.competitor.Competitor
import com.brandforge.app.domain.competitor.CompetitorContent
import com.brandforge.app.domain.competitor.CompetitorInsight

data class CompetitorUiState(
    val creatorId: String = "",
    val competitorUrl: String = "",
    val analyzing: Boolean = false,
    val competitors: List<Competitor> = emptyList(),
    val content: List<CompetitorContent> = emptyList(),
    val insights: List<CompetitorInsight> = emptyList(),
    val errorMessage: String? = null,
)

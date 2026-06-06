package com.brandforge.app.presentation.trend

import com.brandforge.app.domain.trend.TrendOpportunity

data class TrendRadarUiState(
    val creatorId: String = "",
    val scanning: Boolean = false,
    val opportunities: List<TrendOpportunity> = emptyList(),
    val savedOpportunityIds: Set<String> = emptySet(),
    val signalCount: Int = 0,
    val errorMessage: String? = null,
    val saveMessage: String? = null,
)

package com.brandforge.app.domain.trend

import kotlinx.coroutines.flow.Flow

interface TrendRepository {
    fun observeOpportunities(creatorId: String, limit: Int = 20): Flow<List<TrendOpportunity>>
    suspend fun fetchSignals(query: TrendSignalQuery): List<TrendSignal>
    suspend fun persistSignals(signals: List<TrendSignal>): List<TrendSignal>
    suspend fun persistOpportunities(opportunities: List<TrendOpportunity>): List<TrendOpportunity>
}

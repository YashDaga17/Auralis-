package com.brandforge.app.data.trend

import com.brandforge.app.core.database.dao.TrendOpportunityDao
import com.brandforge.app.core.database.dao.TrendSignalDao
import com.brandforge.app.domain.trend.TrendOpportunity
import com.brandforge.app.domain.trend.TrendSignal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomTrendLocalDataSource @Inject constructor(
    private val trendSignalDao: TrendSignalDao,
    private val trendOpportunityDao: TrendOpportunityDao,
) {
    fun observeOpportunities(creatorId: String, limit: Int): Flow<List<TrendOpportunity>> =
        trendOpportunityDao.observeLatestByCreator(creatorId, limit).map { rows ->
            rows.map { it.toDomain() }
        }

    suspend fun upsertSignals(signals: List<TrendSignal>): List<TrendSignal> {
        if (signals.isNotEmpty()) {
            trendSignalDao.upsertAll(signals.map { it.toEntity() })
        }
        return signals
    }

    suspend fun upsertOpportunities(opportunities: List<TrendOpportunity>): List<TrendOpportunity> {
        if (opportunities.isNotEmpty()) {
            trendOpportunityDao.upsertAll(opportunities.map { it.toEntity() })
        }
        return opportunities
    }
}

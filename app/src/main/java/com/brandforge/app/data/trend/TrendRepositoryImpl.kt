package com.brandforge.app.data.trend

import com.brandforge.app.domain.trend.TrendOpportunity
import com.brandforge.app.domain.trend.TrendRepository
import com.brandforge.app.domain.trend.TrendSignal
import com.brandforge.app.domain.trend.TrendSignalQuery
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrendRepositoryImpl @Inject constructor(
    private val localDataSource: RoomTrendLocalDataSource,
    private val firecrawlTrendDataSource: FirecrawlTrendDataSource,
    private val youTubeTrendDataSource: YouTubeTrendDataSource,
) : TrendRepository {
    override fun observeOpportunities(creatorId: String, limit: Int): Flow<List<TrendOpportunity>> =
        localDataSource.observeOpportunities(creatorId, limit)

    override suspend fun fetchSignals(query: TrendSignalQuery): List<TrendSignal> {
        val firecrawlSignals = runCatching {
            firecrawlTrendDataSource.fetchSignals(query)
        }.getOrDefault(emptyList())
        val youTubeSignals = runCatching {
            youTubeTrendDataSource.fetchSignals(query)
        }.getOrDefault(emptyList())

        return (firecrawlSignals + youTubeSignals)
            .filter { it.title.isNotBlank() && it.sourceUrl.isNotBlank() }
            .distinctBy { it.sourceUrl }
    }

    override suspend fun persistSignals(signals: List<TrendSignal>): List<TrendSignal> =
        localDataSource.upsertSignals(signals)

    override suspend fun persistOpportunities(opportunities: List<TrendOpportunity>): List<TrendOpportunity> =
        localDataSource.upsertOpportunities(opportunities)
}

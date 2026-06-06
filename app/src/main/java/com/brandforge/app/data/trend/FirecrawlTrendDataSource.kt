package com.brandforge.app.data.trend

import com.brandforge.app.data.trend.firecrawl.FirecrawlSearchRequest
import com.brandforge.app.data.trend.firecrawl.FirecrawlTrendApi
import com.brandforge.app.domain.trend.TrendSignal
import com.brandforge.app.domain.trend.TrendSignalQuery
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirecrawlTrendDataSource @Inject constructor(
    private val api: FirecrawlTrendApi,
    private val gson: Gson,
) {
    suspend fun fetchSignals(query: TrendSignalQuery): List<TrendSignal> {
        val observedAt = System.currentTimeMillis()
        val response = api.search(
            FirecrawlSearchRequest(
                query = query.query.take(MaxSearchQueryLength),
                limit = query.limit.coerceIn(1, MaxResults),
            ),
        )
        return response.data?.web.orEmpty().mapIndexedNotNull { index, result ->
            val sourceUrl = result.url ?: result.metadata?.sourceURL ?: result.metadata?.url ?: return@mapIndexedNotNull null
            val title = result.title ?: result.metadata?.title ?: return@mapIndexedNotNull null
            TrendSignal(
                id = stableTrendSignalId("firecrawl", sourceUrl),
                creatorId = query.creatorId,
                sourceUrl = sourceUrl,
                sourcePlatform = "Firecrawl Web",
                title = title.cleanText(),
                summary = (result.description ?: result.metadata?.description ?: result.markdown.orEmpty())
                    .cleanText()
                    .take(MaxSummaryLength),
                sourceRank = index + 1,
                observedAt = observedAt,
                publishedAt = null,
                rawPayloadJson = gson.toJson(result),
            )
        }
    }

    private companion object {
        const val MaxResults = 10
        const val MaxSearchQueryLength = 500
        const val MaxSummaryLength = 700
    }
}

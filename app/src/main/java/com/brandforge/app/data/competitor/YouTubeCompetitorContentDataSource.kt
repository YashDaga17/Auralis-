package com.brandforge.app.data.competitor

import com.brandforge.app.data.trend.youtube.YouTubeSearchItem
import com.brandforge.app.data.trend.youtube.YouTubeTrendApi
import com.brandforge.app.domain.competitor.Competitor
import com.brandforge.app.domain.competitor.CompetitorContentInput
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeCompetitorContentDataSource @Inject constructor(
    private val api: YouTubeTrendApi,
    private val gson: Gson,
) {
    suspend fun fetchContent(
        competitor: Competitor,
        limit: Int,
    ): List<CompetitorContentInput> {
        val observedAt = System.currentTimeMillis()
        val response = api.search(
            query = competitor.url.toCompetitorSearchQuery(competitor.name),
            maxResults = limit.coerceIn(1, MaxResults),
            order = "date",
        )
        return response.items.mapIndexedNotNull { index, item ->
            item.toCompetitorContent(
                competitor = competitor,
                observedAt = observedAt,
                rank = index + 1,
            )
        }
    }

    private fun YouTubeSearchItem.toCompetitorContent(
        competitor: Competitor,
        observedAt: Long,
        rank: Int,
    ): CompetitorContentInput? {
        val videoId = id?.videoId ?: return null
        val snippet = snippet ?: return null
        val title = snippet.title?.cleanCompetitorText().orEmpty()
        if (title.isBlank()) return null
        val sourceUrl = "https://www.youtube.com/watch?v=$videoId"
        return CompetitorContentInput(
            id = stableCompetitorContentId(competitor.id, sourceUrl),
            competitorId = competitor.id,
            creatorId = competitor.creatorId,
            title = title,
            summary = snippet.description.orEmpty().cleanCompetitorText().take(MaxSummaryLength),
            publishedAt = snippet.publishedAt?.parseRfc3339Millis(),
            engagementEstimate = "YouTube search rank $rank; engagement stats unavailable from search endpoint",
            sourceUrl = sourceUrl,
            rawPayloadJson = gson.toJson(this),
            observedAt = observedAt,
        )
    }

    private companion object {
        const val MaxResults = 12
        const val MaxSummaryLength = 700
    }
}

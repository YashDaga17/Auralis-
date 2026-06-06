package com.brandforge.app.data.trend

import com.brandforge.app.data.trend.youtube.YouTubeSearchItem
import com.brandforge.app.data.trend.youtube.YouTubeTrendApi
import com.brandforge.app.domain.trend.TrendSignal
import com.brandforge.app.domain.trend.TrendSignalQuery
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeTrendDataSource @Inject constructor(
    private val api: YouTubeTrendApi,
    private val gson: Gson,
) {
    suspend fun fetchSignals(query: TrendSignalQuery): List<TrendSignal> {
        val observedAt = System.currentTimeMillis()
        val response = api.search(
            query = query.query.take(MaxSearchQueryLength),
            maxResults = query.limit.coerceIn(1, MaxResults),
            publishedAfter = sevenDaysAgoRfc3339(),
        )
        return response.items.mapIndexedNotNull { index, item ->
            item.toTrendSignal(
                creatorId = query.creatorId,
                observedAt = observedAt,
                sourceRank = index + 1,
            )
        }
    }

    private fun YouTubeSearchItem.toTrendSignal(
        creatorId: String,
        observedAt: Long,
        sourceRank: Int,
    ): TrendSignal? {
        val videoId = id?.videoId ?: return null
        val snippet = snippet ?: return null
        val title = snippet.title?.cleanText().orEmpty()
        if (title.isBlank()) return null
        val sourceUrl = "https://www.youtube.com/watch?v=$videoId"
        return TrendSignal(
            id = stableTrendSignalId("youtube", sourceUrl),
            creatorId = creatorId,
            sourceUrl = sourceUrl,
            sourcePlatform = "YouTube",
            title = title,
            summary = snippet.description.orEmpty().cleanText().take(MaxSummaryLength),
            sourceRank = sourceRank,
            observedAt = observedAt,
            publishedAt = snippet.publishedAt?.parseRfc3339Millis(),
            rawPayloadJson = gson.toJson(this),
        )
    }

    private fun sevenDaysAgoRfc3339(): String {
        val calendar = Calendar.getInstance(Utc).apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }
        return outputDateFormat().format(calendar.time)
    }

    private fun String.parseRfc3339Millis(): Long? {
        val formats = listOf(
            outputDateFormat(),
            fractionalDateFormat(),
        )
        return formats.firstNotNullOfOrNull { format ->
            runCatching { format.parse(this)?.time }.getOrNull()
        }
    }

    private fun outputDateFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = Utc
        }

    private fun fractionalDateFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = Utc
        }

    private companion object {
        const val MaxResults = 10
        const val MaxSearchQueryLength = 500
        const val MaxSummaryLength = 700
        val Utc: TimeZone = TimeZone.getTimeZone("UTC")
    }
}

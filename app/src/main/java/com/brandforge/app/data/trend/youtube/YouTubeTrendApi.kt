package com.brandforge.app.data.trend.youtube

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeTrendApi {
    @GET("search")
    suspend fun search(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int,
        @Query("type") type: String = "video",
        @Query("order") order: String = "date",
        @Query("regionCode") regionCode: String = "IN",
        @Query("relevanceLanguage") relevanceLanguage: String = "en",
        @Query("safeSearch") safeSearch: String = "moderate",
        @Query("publishedAfter") publishedAfter: String? = null,
    ): YouTubeSearchResponse
}

data class YouTubeSearchResponse(
    val items: List<YouTubeSearchItem> = emptyList(),
)

data class YouTubeSearchItem(
    val id: YouTubeSearchId? = null,
    val snippet: YouTubeSnippet? = null,
)

data class YouTubeSearchId(
    val kind: String? = null,
    val videoId: String? = null,
)

data class YouTubeSnippet(
    val publishedAt: String? = null,
    val channelId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val channelTitle: String? = null,
)

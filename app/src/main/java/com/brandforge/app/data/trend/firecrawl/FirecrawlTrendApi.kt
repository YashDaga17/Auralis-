package com.brandforge.app.data.trend.firecrawl

import retrofit2.http.Body
import retrofit2.http.POST

interface FirecrawlTrendApi {
    @POST("v2/search")
    suspend fun search(
        @Body request: FirecrawlSearchRequest,
    ): FirecrawlSearchResponse

    @POST("v2/scrape")
    suspend fun scrape(
        @Body request: FirecrawlScrapeRequest,
    ): FirecrawlScrapeResponse
}

data class FirecrawlSearchRequest(
    val query: String,
    val limit: Int,
    val sources: List<String> = listOf("web"),
    val country: String = "IN",
    val tbs: String = "sbd:1,qdr:w",
    val timeout: Int = 45_000,
    val ignoreInvalidURLs: Boolean = true,
    val scrapeOptions: FirecrawlScrapeOptions? = FirecrawlScrapeOptions(),
)

data class FirecrawlScrapeOptions(
    val formats: List<String> = listOf("markdown"),
    val onlyMainContent: Boolean = true,
    val removeBase64Images: Boolean = true,
    val blockAds: Boolean = true,
)

data class FirecrawlSearchResponse(
    val success: Boolean? = null,
    val data: FirecrawlSearchData? = null,
    val warning: String? = null,
    val id: String? = null,
    val creditsUsed: Int? = null,
)

data class FirecrawlSearchData(
    val web: List<FirecrawlWebResult> = emptyList(),
)

data class FirecrawlWebResult(
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val markdown: String? = null,
    val metadata: FirecrawlMetadata? = null,
)

data class FirecrawlScrapeRequest(
    val url: String,
    val formats: List<String> = listOf("markdown", "summary", "links"),
    val onlyMainContent: Boolean = true,
    val onlyCleanContent: Boolean = false,
    val mobile: Boolean = true,
    val removeBase64Images: Boolean = true,
    val blockAds: Boolean = true,
    val timeout: Int = 60_000,
)

data class FirecrawlScrapeResponse(
    val success: Boolean? = null,
    val data: FirecrawlScrapeData? = null,
    val warning: String? = null,
    val error: String? = null,
    val creditsUsed: Int? = null,
)

data class FirecrawlScrapeData(
    val markdown: String? = null,
    val summary: String? = null,
    val html: String? = null,
    val rawHtml: String? = null,
    val links: List<String> = emptyList(),
    val metadata: FirecrawlMetadata? = null,
    val warning: String? = null,
)

data class FirecrawlMetadata(
    val title: String? = null,
    val description: String? = null,
    val sourceURL: String? = null,
    val url: String? = null,
    val statusCode: Int? = null,
    val error: String? = null,
)

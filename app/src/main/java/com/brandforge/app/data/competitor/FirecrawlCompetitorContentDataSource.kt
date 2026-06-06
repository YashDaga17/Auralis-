package com.brandforge.app.data.competitor

import com.brandforge.app.data.trend.firecrawl.FirecrawlSearchRequest
import com.brandforge.app.data.trend.firecrawl.FirecrawlScrapeRequest
import com.brandforge.app.data.trend.firecrawl.FirecrawlScrapeResponse
import com.brandforge.app.data.trend.firecrawl.FirecrawlTrendApi
import com.brandforge.app.data.trend.firecrawl.FirecrawlWebResult
import com.brandforge.app.domain.competitor.Competitor
import com.brandforge.app.domain.competitor.CompetitorContentInput
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirecrawlCompetitorContentDataSource @Inject constructor(
    private val api: FirecrawlTrendApi,
    private val gson: Gson,
) {
    suspend fun fetchContent(
        competitor: Competitor,
        limit: Int,
    ): List<CompetitorContentInput> {
        val observedAt = System.currentTimeMillis()
        val exactPage = runCatching {
            api.scrape(
                FirecrawlScrapeRequest(
                    url = competitor.url.normalizedWebUrl(),
                    formats = listOf("markdown", "summary", "links"),
                    onlyCleanContent = true,
                    mobile = true,
                ),
            ).toCompetitorContent(
                competitor = competitor,
                observedAt = observedAt,
            )
        }.getOrNull()
        val response = api.search(
            FirecrawlSearchRequest(
                query = firecrawlQuery(competitor),
                limit = limit.coerceIn(1, MaxResults),
            ),
        )
        val searchContent = response.data?.web.orEmpty().mapIndexedNotNull { index, result ->
            result.toCompetitorContent(
                competitor = competitor,
                observedAt = observedAt,
                rank = index + 1,
            )
        }
        return (listOfNotNull(exactPage) + searchContent)
            .distinctBy { it.sourceUrl }
            .take(limit.coerceIn(1, MaxResults + 1))
    }

    private fun firecrawlQuery(competitor: Competitor): String {
        val siteQuery = competitor.url
            .substringAfter("://", competitor.url)
            .substringBefore("/")
            .takeIf { "." in it }
            ?.let { "site:$it" }
            .orEmpty()
        return listOf(siteQuery, competitor.name, "latest content posts videos insights")
            .joinToString(separator = " ")
            .cleanCompetitorText()
            .take(MaxSearchQueryLength)
    }

    private fun FirecrawlWebResult.toCompetitorContent(
        competitor: Competitor,
        observedAt: Long,
        rank: Int,
    ): CompetitorContentInput? {
        val sourceUrl = url ?: metadata?.sourceURL ?: metadata?.url ?: return null
        val title = (title ?: metadata?.title).orEmpty().cleanCompetitorText()
        if (title.isBlank()) return null
        return CompetitorContentInput(
            id = stableCompetitorContentId(competitor.id, sourceUrl),
            competitorId = competitor.id,
            creatorId = competitor.creatorId,
            title = title,
            summary = (description ?: metadata?.description ?: markdown.orEmpty())
                .cleanCompetitorText()
                .take(MaxSummaryLength),
            publishedAt = null,
            engagementEstimate = "Firecrawl web rank $rank; engagement stats unavailable from web search",
            sourceUrl = sourceUrl,
            rawPayloadJson = gson.toJson(this),
            observedAt = observedAt,
        )
    }

    private fun FirecrawlScrapeResponse.toCompetitorContent(
        competitor: Competitor,
        observedAt: Long,
    ): CompetitorContentInput? {
        val scrapeData = data ?: return null
        val sourceUrl = scrapeData.metadata?.sourceURL ?: scrapeData.metadata?.url ?: competitor.url
        val title = (scrapeData.metadata?.title ?: competitor.name).cleanCompetitorText()
        val summary = listOfNotNull(
            scrapeData.summary,
            scrapeData.metadata?.description,
            scrapeData.markdown,
            scrapeData.links.take(12).takeIf { it.isNotEmpty() }?.joinToString(prefix = "Links: ", separator = " | "),
        ).joinToString(separator = "\n")
            .cleanCompetitorText()
            .take(MaxSummaryLength)
        if (title.isBlank() && summary.isBlank()) return null
        return CompetitorContentInput(
            id = stableCompetitorContentId(competitor.id, sourceUrl),
            competitorId = competitor.id,
            creatorId = competitor.creatorId,
            title = title.ifBlank { competitor.name },
            summary = summary,
            publishedAt = null,
            engagementEstimate = "Firecrawl exact URL scrape; engagement stats unavailable",
            sourceUrl = sourceUrl,
            rawPayloadJson = gson.toJson(this),
            observedAt = observedAt,
        )
    }

    private fun String.normalizedWebUrl(): String {
        val trimmed = trim()
        return when {
            trimmed.startsWith("http://", ignoreCase = true) -> trimmed
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> "https://$trimmed"
        }
    }

    private companion object {
        const val MaxResults = 12
        const val MaxSearchQueryLength = 500
        const val MaxSummaryLength = 700
    }
}

package com.brandforge.app.domain.competitor

import kotlinx.coroutines.flow.Flow

interface CompetitorRepository {
    fun observeCompetitors(creatorId: String, limit: Int = 30): Flow<List<Competitor>>
    fun observeInsights(creatorId: String, limit: Int = 30): Flow<List<CompetitorInsight>>
    fun observeContent(competitorId: String, limit: Int = 30): Flow<List<CompetitorContent>>
    suspend fun fetchContent(competitor: Competitor, limit: Int = 16): List<CompetitorContentInput>
    suspend fun findCompetitorByUrl(creatorId: String, url: String): Competitor?
    suspend fun upsertCompetitor(input: CompetitorInput): Competitor
    suspend fun upsertContent(inputs: List<CompetitorContentInput>): List<CompetitorContent>
    suspend fun upsertInsights(inputs: List<CompetitorInsightInput>): List<CompetitorInsight>
    suspend fun latestInsights(creatorId: String, limit: Int = 10): List<CompetitorInsight>
    suspend fun latestContent(creatorId: String, limit: Int = 30): List<CompetitorContent>
}

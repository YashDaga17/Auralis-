package com.brandforge.app.data.competitor

import com.brandforge.app.core.database.dao.CompetitorContentDao
import com.brandforge.app.core.database.dao.CompetitorDao
import com.brandforge.app.core.database.dao.CompetitorInsightDao
import com.brandforge.app.domain.competitor.Competitor
import com.brandforge.app.domain.competitor.CompetitorContent
import com.brandforge.app.domain.competitor.CompetitorContentInput
import com.brandforge.app.domain.competitor.CompetitorInput
import com.brandforge.app.domain.competitor.CompetitorInsight
import com.brandforge.app.domain.competitor.CompetitorInsightInput
import com.brandforge.app.domain.competitor.CompetitorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompetitorRepositoryImpl @Inject constructor(
    private val competitorDao: CompetitorDao,
    private val competitorContentDao: CompetitorContentDao,
    private val competitorInsightDao: CompetitorInsightDao,
    private val firecrawlDataSource: FirecrawlCompetitorContentDataSource,
    private val youTubeDataSource: YouTubeCompetitorContentDataSource,
) : CompetitorRepository {
    override fun observeCompetitors(creatorId: String, limit: Int): Flow<List<Competitor>> =
        competitorDao.observeByCreator(creatorId, limit).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeInsights(creatorId: String, limit: Int): Flow<List<CompetitorInsight>> =
        competitorInsightDao.observeByCreator(creatorId, limit).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeContent(competitorId: String, limit: Int): Flow<List<CompetitorContent>> =
        competitorContentDao.observeByCompetitor(competitorId, limit).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun fetchContent(
        competitor: Competitor,
        limit: Int,
    ): List<CompetitorContentInput> {
        val firecrawlContent = runCatching {
            firecrawlDataSource.fetchContent(competitor, limit)
        }.getOrDefault(emptyList())
        val youTubeContent = runCatching {
            youTubeDataSource.fetchContent(competitor, limit)
        }.getOrDefault(emptyList())

        return (firecrawlContent + youTubeContent)
            .filter { it.title.isNotBlank() && it.sourceUrl.isNotBlank() }
            .distinctBy { it.sourceUrl }
    }

    override suspend fun findCompetitorByUrl(creatorId: String, url: String): Competitor? =
        competitorDao.findByUrl(creatorId, url)?.toDomain()

    override suspend fun upsertCompetitor(input: CompetitorInput): Competitor {
        val entity = input.toEntity()
        competitorDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun upsertContent(inputs: List<CompetitorContentInput>): List<CompetitorContent> {
        if (inputs.isNotEmpty()) {
            competitorContentDao.upsertAll(inputs.map { it.toEntity() })
        }
        return inputs.map { it.toEntity().toDomain() }
    }

    override suspend fun upsertInsights(inputs: List<CompetitorInsightInput>): List<CompetitorInsight> {
        if (inputs.isNotEmpty()) {
            competitorInsightDao.upsertAll(inputs.map { it.toEntity() })
        }
        return inputs.map { it.toEntity().toDomain() }
    }

    override suspend fun latestInsights(creatorId: String, limit: Int): List<CompetitorInsight> =
        competitorInsightDao.latestByCreator(creatorId, limit).map { it.toDomain() }

    override suspend fun latestContent(creatorId: String, limit: Int): List<CompetitorContent> =
        competitorContentDao.latestByCreator(creatorId, limit).map { it.toDomain() }
}

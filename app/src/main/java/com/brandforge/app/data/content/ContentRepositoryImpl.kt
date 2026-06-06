package com.brandforge.app.data.content

import com.brandforge.app.core.database.dao.GeneratedContentDao
import com.brandforge.app.domain.content.ContentDraft
import com.brandforge.app.domain.content.ContentDraftInput
import com.brandforge.app.domain.content.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val generatedContentDao: GeneratedContentDao,
) : ContentRepository {
    override fun observeDrafts(creatorId: String, limit: Int): Flow<List<ContentDraft>> =
        generatedContentDao.observeLatestByCreator(creatorId, limit).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun persistDraft(input: ContentDraftInput): ContentDraft {
        val entity = input.toEntity()
        generatedContentDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun latestDrafts(creatorId: String, limit: Int): List<ContentDraft> =
        generatedContentDao.latestByCreator(creatorId, limit).map { it.toDomain() }
}

package com.brandforge.app.data.content

import com.brandforge.app.core.database.dao.ContentMediaArtifactDao
import com.brandforge.app.domain.content.ContentMediaArtifact
import com.brandforge.app.domain.content.ContentMediaArtifactInput
import com.brandforge.app.domain.content.MediaArtifactRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class MediaArtifactRepositoryImpl @Inject constructor(
    private val contentMediaArtifactDao: ContentMediaArtifactDao,
) : MediaArtifactRepository {
    override fun observeArtifacts(creatorId: String, limit: Int): Flow<List<ContentMediaArtifact>> =
        contentMediaArtifactDao.observeLatestByCreator(creatorId, limit).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun persist(input: ContentMediaArtifactInput): ContentMediaArtifact {
        val entity = input.toEntity()
        contentMediaArtifactDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun latestArtifacts(creatorId: String, limit: Int): List<ContentMediaArtifact> =
        contentMediaArtifactDao.latestByCreator(creatorId, limit).map { it.toDomain() }
}

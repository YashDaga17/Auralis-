package com.brandforge.app.data.memory

import com.brandforge.app.core.database.dao.BrandDnaDao
import com.brandforge.app.core.database.dao.ContentSampleDao
import com.brandforge.app.core.database.dao.MemoryShardDao
import com.brandforge.app.core.model.MemoryType
import com.brandforge.app.domain.memory.BrandDna
import com.brandforge.app.domain.memory.BrandDnaInput
import com.brandforge.app.domain.memory.ContentSample
import com.brandforge.app.domain.memory.ContentSampleInput
import com.brandforge.app.domain.memory.MemoryShard
import com.brandforge.app.domain.memory.MemoryShardDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomMemoryLocalDataSource @Inject constructor(
    private val brandDnaDao: BrandDnaDao,
    private val memoryShardDao: MemoryShardDao,
    private val contentSampleDao: ContentSampleDao,
) {
    fun observeBrandDna(creatorId: String): Flow<BrandDna?> =
        brandDnaDao.observeByCreator(creatorId).map { it?.toDomain() }

    fun observeMemory(creatorId: String): Flow<List<MemoryShard>> =
        memoryShardDao.observeByCreator(creatorId).map { rows -> rows.map { it.toDomain() } }

    suspend fun upsertBrandDna(input: BrandDnaInput, now: Long = System.currentTimeMillis()): BrandDna {
        val existing = brandDnaDao.getByCreator(input.creatorId)
        val entity = input.toEntity(now = now, existing = existing)
        brandDnaDao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun upsertMemory(
        draft: MemoryShardDraft,
        now: Long = System.currentTimeMillis(),
    ): MemoryShard {
        val existing = memoryShardDao.getByIds(draft.creatorId, listOf(draft.id)).firstOrNull()
        val entity = draft.toEntity(now = now, existing = existing)
        memoryShardDao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun updateEmbeddingId(memory: MemoryShard, embeddingId: String): MemoryShard {
        val entity = memory.toEntityWithEmbeddingId(
            embeddingId = embeddingId,
            now = System.currentTimeMillis(),
        )
        memoryShardDao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun latestMemories(
        creatorId: String,
        limit: Int,
        types: List<MemoryType>,
    ): List<MemoryShard> {
        val rows = if (types.isEmpty()) {
            memoryShardDao.latestByCreator(creatorId, limit)
        } else {
            memoryShardDao.latestByCreatorAndTypes(
                creatorId = creatorId,
                types = types.map { it.name },
                limit = limit,
            )
        }
        return rows.map { it.toDomain() }
    }

    suspend fun memoriesByIds(creatorId: String, ids: List<String>): List<MemoryShard> =
        if (ids.isEmpty()) {
            emptyList()
        } else {
            memoryShardDao.getByIds(creatorId, ids).map { it.toDomain() }
        }

    suspend fun upsertContentSample(
        input: ContentSampleInput,
        now: Long = System.currentTimeMillis(),
    ): ContentSample {
        val entity = input.toEntity(now)
        contentSampleDao.upsert(entity)
        return entity.toDomain()
    }
}

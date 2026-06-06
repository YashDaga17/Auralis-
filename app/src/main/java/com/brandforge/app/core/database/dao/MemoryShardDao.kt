package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.MemoryShardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryShardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MemoryShardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<MemoryShardEntity>): List<Long>

    @Query("SELECT * FROM memory_shard WHERE creator_id = :creatorId ORDER BY retrieval_weight DESC, updated_at DESC")
    fun observeByCreator(creatorId: String): Flow<List<MemoryShardEntity>>

    @Query("SELECT * FROM memory_shard WHERE creator_id = :creatorId ORDER BY retrieval_weight DESC, updated_at DESC LIMIT :limit")
    suspend fun latestByCreator(creatorId: String, limit: Int): List<MemoryShardEntity>

    @Query("SELECT * FROM memory_shard WHERE creator_id = :creatorId AND type IN (:types) ORDER BY retrieval_weight DESC, updated_at DESC LIMIT :limit")
    suspend fun latestByCreatorAndTypes(
        creatorId: String,
        types: List<String>,
        limit: Int,
    ): List<MemoryShardEntity>

    @Query("SELECT * FROM memory_shard WHERE creator_id = :creatorId AND id IN (:ids)")
    suspend fun getByIds(creatorId: String, ids: List<String>): List<MemoryShardEntity>
}

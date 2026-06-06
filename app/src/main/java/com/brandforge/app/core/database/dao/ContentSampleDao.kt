package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.ContentSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentSampleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ContentSampleEntity): Long

    @Query("SELECT * FROM content_sample WHERE creator_id = :creatorId ORDER BY created_at DESC")
    fun observeByCreator(creatorId: String): Flow<List<ContentSampleEntity>>

    @Query("SELECT * FROM content_sample WHERE creator_id = :creatorId ORDER BY created_at DESC LIMIT :limit")
    suspend fun latestByCreator(creatorId: String, limit: Int): List<ContentSampleEntity>

    @Query("SELECT * FROM content_sample WHERE creator_id = :creatorId AND platform = :platform ORDER BY created_at DESC LIMIT :limit")
    suspend fun latestByCreatorAndPlatform(
        creatorId: String,
        platform: String,
        limit: Int,
    ): List<ContentSampleEntity>
}

package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.ContentDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ContentDraftEntity): Long

    @Query("SELECT * FROM content_draft WHERE creator_id = :creatorId ORDER BY generated_at DESC LIMIT :limit")
    fun observeLatestByCreator(creatorId: String, limit: Int): Flow<List<ContentDraftEntity>>

    @Query("SELECT * FROM content_draft WHERE creator_id = :creatorId ORDER BY generated_at DESC LIMIT :limit")
    suspend fun latestByCreator(creatorId: String, limit: Int): List<ContentDraftEntity>
}

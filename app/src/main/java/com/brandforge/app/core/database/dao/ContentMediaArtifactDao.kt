package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.ContentMediaArtifactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentMediaArtifactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ContentMediaArtifactEntity): Long

    @Query("SELECT * FROM content_media_artifact WHERE creator_id = :creatorId ORDER BY created_at DESC LIMIT :limit")
    fun observeLatestByCreator(creatorId: String, limit: Int): Flow<List<ContentMediaArtifactEntity>>

    @Query("SELECT * FROM content_media_artifact WHERE creator_id = :creatorId ORDER BY created_at DESC LIMIT :limit")
    suspend fun latestByCreator(creatorId: String, limit: Int): List<ContentMediaArtifactEntity>
}

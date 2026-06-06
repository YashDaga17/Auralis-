package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.CompetitorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CompetitorEntity): Long

    @Query("SELECT * FROM competitor WHERE creator_id = :creatorId ORDER BY last_analyzed DESC, name ASC LIMIT :limit")
    fun observeByCreator(creatorId: String, limit: Int): Flow<List<CompetitorEntity>>

    @Query("SELECT * FROM competitor WHERE creator_id = :creatorId AND url = :url LIMIT 1")
    suspend fun findByUrl(creatorId: String, url: String): CompetitorEntity?

    @Query("SELECT * FROM competitor WHERE id = :competitorId LIMIT 1")
    suspend fun getById(competitorId: String): CompetitorEntity?
}

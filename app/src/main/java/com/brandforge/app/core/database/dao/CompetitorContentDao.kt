package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.CompetitorContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitorContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<CompetitorContentEntity>): List<Long>

    @Query("SELECT * FROM competitor_content WHERE competitor_id = :competitorId ORDER BY published_at DESC, observed_at DESC LIMIT :limit")
    fun observeByCompetitor(competitorId: String, limit: Int): Flow<List<CompetitorContentEntity>>

    @Query("SELECT * FROM competitor_content WHERE competitor_id = :competitorId ORDER BY published_at DESC, observed_at DESC LIMIT :limit")
    suspend fun latestByCompetitor(competitorId: String, limit: Int): List<CompetitorContentEntity>

    @Query("SELECT * FROM competitor_content WHERE creator_id = :creatorId ORDER BY published_at DESC, observed_at DESC LIMIT :limit")
    suspend fun latestByCreator(creatorId: String, limit: Int): List<CompetitorContentEntity>
}

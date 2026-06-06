package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.CompetitorInsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitorInsightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<CompetitorInsightEntity>): List<Long>

    @Query("SELECT * FROM competitor_insight WHERE creator_id = :creatorId ORDER BY confidence DESC, opportunity_score DESC, created_at DESC LIMIT :limit")
    fun observeByCreator(creatorId: String, limit: Int): Flow<List<CompetitorInsightEntity>>

    @Query("SELECT * FROM competitor_insight WHERE creator_id = :creatorId ORDER BY confidence DESC, opportunity_score DESC, created_at DESC LIMIT :limit")
    suspend fun latestByCreator(creatorId: String, limit: Int): List<CompetitorInsightEntity>

    @Query("SELECT * FROM competitor_insight WHERE competitor_id = :competitorId ORDER BY confidence DESC, opportunity_score DESC, created_at DESC LIMIT :limit")
    suspend fun latestByCompetitor(competitorId: String, limit: Int): List<CompetitorInsightEntity>
}

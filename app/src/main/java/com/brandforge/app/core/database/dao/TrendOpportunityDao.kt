package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.TrendOpportunityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrendOpportunityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<TrendOpportunityEntity>): List<Long>

    @Query("SELECT * FROM trend_opportunity WHERE creator_id = :creatorId ORDER BY opportunity_score DESC, created_at DESC LIMIT :limit")
    fun observeLatestByCreator(creatorId: String, limit: Int): Flow<List<TrendOpportunityEntity>>

    @Query("SELECT * FROM trend_opportunity WHERE creator_id = :creatorId ORDER BY opportunity_score DESC, created_at DESC LIMIT :limit")
    suspend fun latestByCreator(creatorId: String, limit: Int): List<TrendOpportunityEntity>
}

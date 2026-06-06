package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.TrendSignalEntity

@Dao
interface TrendSignalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<TrendSignalEntity>): List<Long>

    @Query("SELECT * FROM trend_signal WHERE creator_id = :creatorId ORDER BY observed_at DESC LIMIT :limit")
    suspend fun latestByCreator(creatorId: String, limit: Int): List<TrendSignalEntity>
}

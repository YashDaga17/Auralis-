package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.DebugErrorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebugErrorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DebugErrorEntity): Long

    @Query("SELECT * FROM debug_error_log ORDER BY timestamp DESC LIMIT :limit")
    fun observeLatest(limit: Int): Flow<List<DebugErrorEntity>>

    @Query("SELECT * FROM debug_error_log ORDER BY timestamp DESC LIMIT :limit")
    suspend fun latest(limit: Int): List<DebugErrorEntity>

    @Query("DELETE FROM debug_error_log")
    suspend fun clear(): Int
}

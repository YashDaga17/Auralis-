package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.DebugChecklistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebugChecklistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(items: List<DebugChecklistItemEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DebugChecklistItemEntity): Long

    @Query("SELECT * FROM debug_checklist_item ORDER BY criticality DESC, label ASC")
    fun observeAll(): Flow<List<DebugChecklistItemEntity>>

    @Query("SELECT * FROM debug_checklist_item WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DebugChecklistItemEntity?
}

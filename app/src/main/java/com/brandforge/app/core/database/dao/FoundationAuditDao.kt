package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.FoundationAuditEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoundationAuditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FoundationAuditEntity): Long

    @Query("SELECT * FROM foundation_audit WHERE id = :id")
    fun observe(id: String = FoundationAuditEntity.LatestId): Flow<FoundationAuditEntity?>

    @Query("SELECT * FROM foundation_audit WHERE id = :id")
    suspend fun latest(id: String = FoundationAuditEntity.LatestId): FoundationAuditEntity?
}

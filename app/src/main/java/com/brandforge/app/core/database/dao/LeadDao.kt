package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.LeadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LeadEntity): Long

    @Query(
        """
        SELECT * FROM lead_detection
        WHERE creator_id = :creatorId
        ORDER BY
            CASE priority
                WHEN 'Critical' THEN 0
                WHEN 'High' THEN 1
                WHEN 'Medium' THEN 2
                ELSE 3
            END,
            confidence DESC,
            classified_at DESC
        LIMIT :limit
        """,
    )
    fun observeInbox(creatorId: String, limit: Int): Flow<List<LeadEntity>>

    @Query(
        """
        SELECT * FROM lead_detection
        WHERE creator_id = :creatorId
        ORDER BY classified_at DESC
        LIMIT :limit
        """,
    )
    suspend fun latestByCreator(creatorId: String, limit: Int): List<LeadEntity>
}

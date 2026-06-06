package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.TwinChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TwinChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TwinChatMessageEntity): Long

    @Query(
        """
        SELECT * FROM (
            SELECT * FROM twin_chat_message
            WHERE creator_id = :creatorId
            ORDER BY created_at DESC
            LIMIT :limit
        )
        ORDER BY created_at ASC
        """,
    )
    fun observeByCreator(creatorId: String, limit: Int): Flow<List<TwinChatMessageEntity>>

    @Query("SELECT * FROM twin_chat_message WHERE creator_id = :creatorId ORDER BY created_at DESC LIMIT :limit")
    suspend fun latestByCreator(creatorId: String, limit: Int): List<TwinChatMessageEntity>
}

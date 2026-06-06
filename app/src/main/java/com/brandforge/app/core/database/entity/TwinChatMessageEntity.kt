package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "twin_chat_message",
    indices = [
        Index(
            value = ["creator_id", "created_at"],
            name = "idx_twin_chat_creator_created",
        ),
    ],
)
data class TwinChatMessageEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    val role: String,
    val message: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "memory_ids")
    val memoryIds: String,
    @ColumnInfo(name = "trend_ids")
    val trendIds: String,
    @ColumnInfo(name = "opportunity_ids")
    val opportunityIds: String,
    @ColumnInfo(name = "content_draft_ids")
    val contentDraftIds: String,
)

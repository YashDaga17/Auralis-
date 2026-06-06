package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memory_shard",
    indices = [
        Index(
            value = ["creator_id", "type"],
            name = "idx_memory_creator_type",
        ),
    ],
)
data class MemoryShardEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    val type: String,
    val title: String,
    val summary: String,
    @ColumnInfo(name = "source_uri")
    val sourceUri: String?,
    @ColumnInfo(name = "embedding_id")
    val embeddingId: String?,
    @ColumnInfo(name = "retrieval_weight")
    val retrievalWeight: Float,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

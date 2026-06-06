package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "content_draft",
    indices = [
        Index(
            value = ["creator_id", "generated_at"],
            name = "idx_content_draft_creator_generated",
        ),
        Index(
            value = ["source_trend_id"],
            name = "idx_content_draft_source_trend",
        ),
    ],
)
data class ContentDraftEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    val title: String,
    val content: String,
    val format: String,
    @ColumnInfo(name = "generated_at")
    val generatedAt: Long,
    @ColumnInfo(name = "source_trend_id")
    val sourceTrendId: String,
    @ColumnInfo(name = "opportunity_score")
    val opportunityScore: Float,
    @ColumnInfo(name = "memory_ids_used")
    val memoryIdsUsed: String,
    @ColumnInfo(name = "why_generated")
    val whyGenerated: String,
)

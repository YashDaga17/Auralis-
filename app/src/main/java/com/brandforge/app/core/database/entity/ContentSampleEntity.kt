package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "content_sample",
    indices = [
        Index(
            value = ["creator_id", "platform"],
            name = "idx_content_creator_platform",
        ),
    ],
)
data class ContentSampleEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    val platform: String,
    val body: String,
    @ColumnInfo(name = "performance_json")
    val performanceJson: String,
    @ColumnInfo(name = "style_features_json")
    val styleFeaturesJson: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)

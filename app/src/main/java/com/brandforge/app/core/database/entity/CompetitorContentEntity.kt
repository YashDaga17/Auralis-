package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "competitor_content",
    indices = [
        Index(
            value = ["competitor_id", "published_at"],
            name = "idx_competitor_content_competitor_published",
        ),
        Index(
            value = ["creator_id", "published_at"],
            name = "idx_competitor_content_creator_published",
        ),
    ],
)
data class CompetitorContentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "competitor_id")
    val competitorId: String,
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    val title: String,
    val summary: String,
    @ColumnInfo(name = "published_at")
    val publishedAt: Long?,
    @ColumnInfo(name = "engagement_estimate")
    val engagementEstimate: String,
    @ColumnInfo(name = "source_url")
    val sourceUrl: String,
    @ColumnInfo(name = "raw_payload_json")
    val rawPayloadJson: String,
    @ColumnInfo(name = "observed_at")
    val observedAt: Long,
)

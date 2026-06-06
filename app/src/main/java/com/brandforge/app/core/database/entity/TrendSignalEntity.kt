package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trend_signal",
    indices = [
        Index(
            value = ["creator_id", "source_platform"],
            name = "idx_trend_signal_creator_platform",
        ),
    ],
)
data class TrendSignalEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    @ColumnInfo(name = "source_url")
    val sourceUrl: String,
    @ColumnInfo(name = "source_platform")
    val sourcePlatform: String,
    val title: String,
    val summary: String,
    @ColumnInfo(name = "source_rank")
    val sourceRank: Int,
    @ColumnInfo(name = "observed_at")
    val observedAt: Long,
    @ColumnInfo(name = "published_at")
    val publishedAt: Long?,
    @ColumnInfo(name = "raw_payload_json")
    val rawPayloadJson: String,
)

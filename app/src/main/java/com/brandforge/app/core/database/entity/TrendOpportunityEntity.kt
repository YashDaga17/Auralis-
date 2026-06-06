package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trend_opportunity",
    indices = [
        Index(
            value = ["creator_id", "opportunity_score"],
            name = "idx_trend_opportunity_creator_score",
        ),
    ],
)
data class TrendOpportunityEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    @ColumnInfo(name = "signal_id")
    val signalId: String,
    @ColumnInfo(name = "source_url")
    val sourceUrl: String,
    @ColumnInfo(name = "source_platform")
    val sourcePlatform: String,
    val title: String,
    val summary: String,
    @ColumnInfo(name = "velocity_score")
    val velocityScore: Float,
    @ColumnInfo(name = "freshness_score")
    val freshnessScore: Float,
    @ColumnInfo(name = "brand_fit_score")
    val brandFitScore: Float,
    @ColumnInfo(name = "opportunity_score")
    val opportunityScore: Float,
    @ColumnInfo(name = "recommended_format")
    val recommendedFormat: String,
    val rationale: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)

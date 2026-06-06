package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "competitor_insight",
    indices = [
        Index(
            value = ["creator_id", "confidence", "created_at"],
            name = "idx_competitor_insight_creator_confidence",
        ),
        Index(
            value = ["competitor_id", "created_at"],
            name = "idx_competitor_insight_competitor_created",
        ),
    ],
)
data class CompetitorInsightEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "competitor_id")
    val competitorId: String,
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    val pattern: String,
    val frequency: String,
    val gap: String,
    val recommendation: String,
    val confidence: Float,
    val reasoning: String,
    @ColumnInfo(name = "recommended_content_format")
    val recommendedContentFormat: String,
    @ColumnInfo(name = "recommended_hook")
    val recommendedHook: String,
    @ColumnInfo(name = "recommended_angle")
    val recommendedAngle: String,
    @ColumnInfo(name = "opportunity_score")
    val opportunityScore: Float,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)

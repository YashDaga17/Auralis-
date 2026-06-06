package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lead_detection",
    indices = [
        Index(
            value = ["creator_id", "priority", "classified_at"],
            name = "idx_lead_creator_priority",
        ),
        Index(
            value = ["creator_id", "classification"],
            name = "idx_lead_creator_classification",
        ),
    ],
)
data class LeadEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    @ColumnInfo(name = "source_type")
    val sourceType: String,
    val platform: String,
    @ColumnInfo(name = "author_handle")
    val authorHandle: String,
    val text: String,
    val classification: String,
    val confidence: Float,
    @ColumnInfo(name = "suggested_reply")
    val suggestedReply: String,
    val priority: String,
    val reason: String,
    @ColumnInfo(name = "received_at")
    val receivedAt: Long,
    @ColumnInfo(name = "classified_at")
    val classifiedAt: Long,
    @ColumnInfo(name = "raw_model_response")
    val rawModelResponse: String,
)

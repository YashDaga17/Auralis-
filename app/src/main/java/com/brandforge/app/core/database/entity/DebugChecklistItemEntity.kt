package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debug_checklist_item",
    indices = [
        Index(
            value = ["status", "updated_at"],
            name = "idx_debug_checklist_status_updated",
        ),
    ],
)
data class DebugChecklistItemEntity(
    @PrimaryKey
    val id: String,
    val label: String,
    val status: String,
    @ColumnInfo(name = "expected_result")
    val expectedResult: String,
    @ColumnInfo(name = "failure_conditions")
    val failureConditions: String,
    val criticality: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    val notes: String,
)

package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debug_error_log",
    indices = [
        Index(
            value = ["timestamp"],
            name = "idx_debug_error_timestamp",
        ),
        Index(
            value = ["feature", "timestamp"],
            name = "idx_debug_error_feature_timestamp",
        ),
    ],
)
data class DebugErrorEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val feature: String,
    val screen: String,
    val message: String,
    @ColumnInfo(name = "stack_trace")
    val stackTrace: String,
    val severity: String,
)

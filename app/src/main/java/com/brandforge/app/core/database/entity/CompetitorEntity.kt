package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "competitor",
    indices = [
        Index(
            value = ["creator_id", "url"],
            name = "idx_competitor_creator_url",
            unique = true,
        ),
        Index(
            value = ["creator_id", "last_analyzed"],
            name = "idx_competitor_creator_analyzed",
        ),
    ],
)
data class CompetitorEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    val name: String,
    val platform: String,
    val url: String,
    @ColumnInfo(name = "last_analyzed")
    val lastAnalyzed: Long?,
)

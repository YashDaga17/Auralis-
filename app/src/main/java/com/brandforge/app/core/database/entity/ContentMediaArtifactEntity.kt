package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "content_media_artifact",
    indices = [
        Index(
            value = ["creator_id", "created_at"],
            name = "idx_content_media_creator_created",
        ),
        Index(
            value = ["source_draft_id"],
            name = "idx_content_media_source_draft",
        ),
    ],
)
data class ContentMediaArtifactEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    val type: String,
    val prompt: String,
    @ColumnInfo(name = "local_uri")
    val localUri: String?,
    @ColumnInfo(name = "remote_uri")
    val remoteUri: String?,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    val model: String,
    val status: String,
    @ColumnInfo(name = "error_message")
    val errorMessage: String?,
    @ColumnInfo(name = "source_draft_id")
    val sourceDraftId: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)

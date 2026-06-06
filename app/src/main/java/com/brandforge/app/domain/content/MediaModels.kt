package com.brandforge.app.domain.content

enum class MediaArtifactType(
    val label: String,
) {
    Image("Image"),
    Video("Video"),
}

enum class MediaArtifactStatus {
    Ready,
    RemoteReady,
    Failed,
}

data class ContentMediaArtifact(
    val id: String,
    val creatorId: String,
    val type: MediaArtifactType,
    val prompt: String,
    val localUri: String?,
    val remoteUri: String?,
    val mimeType: String,
    val model: String,
    val status: MediaArtifactStatus,
    val errorMessage: String?,
    val sourceDraftId: String?,
    val createdAt: Long,
)

data class ContentMediaArtifactInput(
    val id: String,
    val creatorId: String,
    val type: MediaArtifactType,
    val prompt: String,
    val localUri: String?,
    val remoteUri: String?,
    val mimeType: String,
    val model: String,
    val status: MediaArtifactStatus,
    val errorMessage: String?,
    val sourceDraftId: String?,
    val createdAt: Long,
)

data class MediaGenerationRequest(
    val creatorId: String,
    val prompt: String,
    val sourceDraftId: String? = null,
)

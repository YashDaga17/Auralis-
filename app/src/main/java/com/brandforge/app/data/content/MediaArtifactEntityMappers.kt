package com.brandforge.app.data.content

import com.brandforge.app.core.database.entity.ContentMediaArtifactEntity
import com.brandforge.app.domain.content.ContentMediaArtifact
import com.brandforge.app.domain.content.ContentMediaArtifactInput
import com.brandforge.app.domain.content.MediaArtifactStatus
import com.brandforge.app.domain.content.MediaArtifactType

fun ContentMediaArtifactEntity.toDomain(): ContentMediaArtifact =
    ContentMediaArtifact(
        id = id,
        creatorId = creatorId,
        type = runCatching { MediaArtifactType.valueOf(type) }.getOrDefault(MediaArtifactType.Image),
        prompt = prompt,
        localUri = localUri,
        remoteUri = remoteUri,
        mimeType = mimeType,
        model = model,
        status = runCatching { MediaArtifactStatus.valueOf(status) }.getOrDefault(MediaArtifactStatus.Failed),
        errorMessage = errorMessage,
        sourceDraftId = sourceDraftId,
        createdAt = createdAt,
    )

fun ContentMediaArtifactInput.toEntity(): ContentMediaArtifactEntity =
    ContentMediaArtifactEntity(
        id = id,
        creatorId = creatorId,
        type = type.name,
        prompt = prompt,
        localUri = localUri,
        remoteUri = remoteUri,
        mimeType = mimeType,
        model = model,
        status = status.name,
        errorMessage = errorMessage,
        sourceDraftId = sourceDraftId,
        createdAt = createdAt,
    )

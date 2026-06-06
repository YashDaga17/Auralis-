package com.brandforge.app.data.content

import com.brandforge.app.core.database.entity.ContentDraftEntity
import com.brandforge.app.domain.content.ContentDraft
import com.brandforge.app.domain.content.ContentDraftInput
import com.brandforge.app.domain.content.ContentFormat

fun ContentDraftEntity.toDomain(): ContentDraft =
    ContentDraft(
        id = id,
        creatorId = creatorId,
        title = title,
        content = content,
        format = ContentFormat.valueOf(format),
        generatedAt = generatedAt,
        sourceTrendId = sourceTrendId,
        opportunityScore = opportunityScore,
        memoryIdsUsed = memoryIdsUsed.split(",").filter { it.isNotBlank() },
        whyGenerated = whyGenerated,
    )

fun ContentDraftInput.toEntity(): ContentDraftEntity =
    ContentDraftEntity(
        id = id,
        creatorId = creatorId,
        title = title,
        content = content,
        format = format.name,
        generatedAt = generatedAt,
        sourceTrendId = sourceTrendId,
        opportunityScore = opportunityScore,
        memoryIdsUsed = memoryIdsUsed.joinToString(separator = ","),
        whyGenerated = whyGenerated,
    )

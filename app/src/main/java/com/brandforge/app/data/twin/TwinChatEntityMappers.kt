package com.brandforge.app.data.twin

import com.brandforge.app.core.database.entity.TwinChatMessageEntity
import com.brandforge.app.domain.twin.TwinChatMessage
import com.brandforge.app.domain.twin.TwinChatMessageDraft
import com.brandforge.app.domain.twin.TwinChatRole

fun TwinChatMessageEntity.toDomain(): TwinChatMessage =
    TwinChatMessage(
        id = id,
        creatorId = creatorId,
        role = TwinChatRole.valueOf(role),
        message = message,
        createdAt = createdAt,
        memoryIds = memoryIds.toIdList(),
        trendIds = trendIds.toIdList(),
        opportunityIds = opportunityIds.toIdList(),
        contentDraftIds = contentDraftIds.toIdList(),
    )

fun TwinChatMessageDraft.toEntity(): TwinChatMessageEntity =
    TwinChatMessageEntity(
        id = id,
        creatorId = creatorId,
        role = role.name,
        message = message,
        createdAt = createdAt,
        memoryIds = memoryIds.joinToString(separator = ","),
        trendIds = trendIds.joinToString(separator = ","),
        opportunityIds = opportunityIds.joinToString(separator = ","),
        contentDraftIds = contentDraftIds.joinToString(separator = ","),
    )

private fun String.toIdList(): List<String> =
    split(",").map { it.trim() }.filter { it.isNotBlank() }

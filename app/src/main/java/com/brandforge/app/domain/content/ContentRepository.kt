package com.brandforge.app.domain.content

import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun observeDrafts(creatorId: String, limit: Int = 30): Flow<List<ContentDraft>>
    suspend fun persistDraft(input: ContentDraftInput): ContentDraft
    suspend fun latestDrafts(creatorId: String, limit: Int = 30): List<ContentDraft>
}

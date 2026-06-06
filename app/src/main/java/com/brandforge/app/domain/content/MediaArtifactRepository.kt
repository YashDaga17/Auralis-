package com.brandforge.app.domain.content

import kotlinx.coroutines.flow.Flow

interface MediaArtifactRepository {
    fun observeArtifacts(creatorId: String, limit: Int = 30): Flow<List<ContentMediaArtifact>>
    suspend fun persist(input: ContentMediaArtifactInput): ContentMediaArtifact
    suspend fun latestArtifacts(creatorId: String, limit: Int = 30): List<ContentMediaArtifact>
}

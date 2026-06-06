package com.brandforge.app.domain.memory

import com.brandforge.app.core.model.MemoryType

data class BrandDna(
    val creatorId: String,
    val creatorName: String,
    val archetype: String,
    val voiceRulesJson: String,
    val bannedClaimsJson: String,
    val businessGoalsJson: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class BrandDnaInput(
    val creatorId: String,
    val creatorName: String,
    val archetype: String,
    val voiceRulesJson: String,
    val bannedClaimsJson: String,
    val businessGoalsJson: String,
)

data class MemoryShard(
    val id: String,
    val creatorId: String,
    val type: MemoryType,
    val title: String,
    val summary: String,
    val sourceUri: String?,
    val embeddingId: String?,
    val retrievalWeight: Float,
    val createdAt: Long,
    val updatedAt: Long,
)

data class MemoryShardDraft(
    val id: String,
    val creatorId: String,
    val type: MemoryType,
    val title: String,
    val summary: String,
    val sourceUri: String?,
    val retrievalWeight: Float,
)

data class MemoryQuery(
    val creatorId: String,
    val query: String,
    val limit: Int = 8,
    val types: List<MemoryType> = emptyList(),
)

data class ContentSample(
    val id: String,
    val creatorId: String,
    val platform: String,
    val body: String,
    val performanceJson: String,
    val styleFeaturesJson: String,
    val createdAt: Long,
)

data class ContentSampleInput(
    val id: String,
    val creatorId: String,
    val platform: String,
    val body: String,
    val performanceJson: String,
    val styleFeaturesJson: String,
)

data class EmbeddingVector(
    val values: List<Float>,
)

data class MemoryRetrievalResult(
    val memories: List<MemoryShard>,
    val usedRemoteVectorSearch: Boolean,
)

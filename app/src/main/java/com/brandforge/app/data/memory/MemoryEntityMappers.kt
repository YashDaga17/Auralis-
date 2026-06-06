package com.brandforge.app.data.memory

import com.brandforge.app.core.database.entity.BrandDnaEntity
import com.brandforge.app.core.database.entity.ContentSampleEntity
import com.brandforge.app.core.database.entity.MemoryShardEntity
import com.brandforge.app.core.model.MemoryType
import com.brandforge.app.domain.memory.BrandDna
import com.brandforge.app.domain.memory.BrandDnaInput
import com.brandforge.app.domain.memory.ContentSample
import com.brandforge.app.domain.memory.ContentSampleInput
import com.brandforge.app.domain.memory.MemoryShard
import com.brandforge.app.domain.memory.MemoryShardDraft

fun BrandDnaEntity.toDomain(): BrandDna =
    BrandDna(
        creatorId = creatorId,
        creatorName = creatorName,
        archetype = archetype,
        voiceRulesJson = voiceRulesJson,
        bannedClaimsJson = bannedClaimsJson,
        businessGoalsJson = businessGoalsJson,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun BrandDnaInput.toEntity(now: Long, existing: BrandDnaEntity?): BrandDnaEntity =
    BrandDnaEntity(
        creatorId = creatorId,
        creatorName = creatorName,
        archetype = archetype,
        voiceRulesJson = voiceRulesJson,
        bannedClaimsJson = bannedClaimsJson,
        businessGoalsJson = businessGoalsJson,
        createdAt = existing?.createdAt ?: now,
        updatedAt = now,
    )

fun MemoryShardEntity.toDomain(): MemoryShard =
    MemoryShard(
        id = id,
        creatorId = creatorId,
        type = MemoryType.valueOf(type),
        title = title,
        summary = summary,
        sourceUri = sourceUri,
        embeddingId = embeddingId,
        retrievalWeight = retrievalWeight,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun MemoryShardDraft.toEntity(
    now: Long,
    existing: MemoryShardEntity? = null,
    embeddingId: String? = existing?.embeddingId,
): MemoryShardEntity =
    MemoryShardEntity(
        id = id,
        creatorId = creatorId,
        type = type.name,
        title = title,
        summary = summary,
        sourceUri = sourceUri,
        embeddingId = embeddingId,
        retrievalWeight = retrievalWeight,
        createdAt = existing?.createdAt ?: now,
        updatedAt = now,
    )

fun MemoryShard.toEntityWithEmbeddingId(embeddingId: String, now: Long): MemoryShardEntity =
    MemoryShardEntity(
        id = id,
        creatorId = creatorId,
        type = type.name,
        title = title,
        summary = summary,
        sourceUri = sourceUri,
        embeddingId = embeddingId,
        retrievalWeight = retrievalWeight,
        createdAt = createdAt,
        updatedAt = now,
    )

fun ContentSampleEntity.toDomain(): ContentSample =
    ContentSample(
        id = id,
        creatorId = creatorId,
        platform = platform,
        body = body,
        performanceJson = performanceJson,
        styleFeaturesJson = styleFeaturesJson,
        createdAt = createdAt,
    )

fun ContentSampleInput.toEntity(now: Long): ContentSampleEntity =
    ContentSampleEntity(
        id = id,
        creatorId = creatorId,
        platform = platform,
        body = body,
        performanceJson = performanceJson,
        styleFeaturesJson = styleFeaturesJson,
        createdAt = now,
    )

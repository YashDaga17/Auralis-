package com.brandforge.app.domain.memory

import kotlinx.coroutines.flow.Flow

interface CreatorMemoryRepository {
    fun observeBrandDna(creatorId: String): Flow<BrandDna>
    fun observeMemory(creatorId: String): Flow<List<MemoryShard>>
    suspend fun upsertBrandDna(input: BrandDnaInput)
    suspend fun writeMemory(shard: MemoryShardDraft): MemoryShard
    suspend fun writeContentSample(input: ContentSampleInput): ContentSample
    suspend fun retrieve(query: MemoryQuery): List<MemoryShard>
}

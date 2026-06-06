package com.brandforge.app.data.memory

import com.brandforge.app.core.ai.EmbeddingClient
import com.brandforge.app.core.model.MemoryType
import com.brandforge.app.domain.memory.BrandDna
import com.brandforge.app.domain.memory.BrandDnaInput
import com.brandforge.app.domain.memory.ContentSample
import com.brandforge.app.domain.memory.ContentSampleInput
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryQuery
import com.brandforge.app.domain.memory.MemoryRetrievalEngine
import com.brandforge.app.domain.memory.MemoryShard
import com.brandforge.app.domain.memory.MemoryShardDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreatorMemoryRepositoryImpl @Inject constructor(
    private val localDataSource: RoomMemoryLocalDataSource,
    private val qdrantDataSource: QdrantMemoryRemoteDataSource,
    private val embeddingClient: EmbeddingClient,
    private val retrievalEngine: MemoryRetrievalEngine,
) : CreatorMemoryRepository {
    override fun observeBrandDna(creatorId: String): Flow<BrandDna> =
        localDataSource.observeBrandDna(creatorId).filterNotNull()

    override fun observeMemory(creatorId: String): Flow<List<MemoryShard>> =
        localDataSource.observeMemory(creatorId)

    override suspend fun upsertBrandDna(input: BrandDnaInput) {
        val brandDna = localDataSource.upsertBrandDna(input)
        val brandDnaMemory = localDataSource.upsertMemory(brandDna.toMemoryShardDraft())
        val embedding = runCatching {
            embeddingClient.embed(brandDnaMemory.toRetrievalText())
        }.getOrNull() ?: return

        val remoteId = runCatching {
            qdrantDataSource.upsert(brandDnaMemory, embedding)
        }.getOrNull() ?: return

        localDataSource.updateEmbeddingId(brandDnaMemory, remoteId)
    }

    override suspend fun writeMemory(shard: MemoryShardDraft): MemoryShard {
        val localMemory = localDataSource.upsertMemory(shard)
        val embedding = runCatching {
            embeddingClient.embed(localMemory.toRetrievalText())
        }.getOrNull() ?: return localMemory

        val remoteId = runCatching {
            qdrantDataSource.upsert(localMemory, embedding)
        }.getOrNull() ?: return localMemory

        return localDataSource.updateEmbeddingId(localMemory, remoteId)
    }

    override suspend fun writeContentSample(input: ContentSampleInput): ContentSample {
        val contentSample = localDataSource.upsertContentSample(input)
        writeMemory(
            MemoryShardDraft(
                id = "content-${contentSample.id}",
                creatorId = contentSample.creatorId,
                type = MemoryType.PastContent,
                title = "${contentSample.platform} content sample",
                summary = contentSample.body,
                sourceUri = null,
                retrievalWeight = 0.72f,
            ),
        )
        return contentSample
    }

    override suspend fun retrieve(query: MemoryQuery): List<MemoryShard> {
        val localCandidates = localDataSource.latestMemories(
            creatorId = query.creatorId,
            limit = (query.limit * 3).coerceAtLeast(query.limit),
            types = query.types,
        )

        val queryEmbedding = runCatching {
            embeddingClient.embed(query.query)
        }.getOrNull()

        val remoteScores = queryEmbedding?.let { embedding ->
            runCatching {
                qdrantDataSource.search(
                    creatorId = query.creatorId,
                    embedding = embedding,
                    limit = (query.limit * 2).coerceAtLeast(query.limit),
                )
            }.getOrDefault(emptyMap())
        }.orEmpty()

        val remoteMemories = localDataSource.memoriesByIds(
            creatorId = query.creatorId,
            ids = remoteScores.keys.toList(),
        )

        return retrievalEngine.rank(
            query = query,
            localMemories = remoteMemories + localCandidates,
            remoteScoresById = remoteScores,
        )
    }

    private fun BrandDna.toRetrievalText(): String =
        listOf(
            creatorName,
            archetype,
            voiceRulesJson,
            bannedClaimsJson,
            businessGoalsJson,
        ).joinToString(separator = "\n")

    private fun BrandDna.toMemoryShardDraft(): MemoryShardDraft =
        MemoryShardDraft(
            id = "brand-dna-$creatorId",
            creatorId = creatorId,
            type = MemoryType.BrandDna,
            title = "Brand DNA Fingerprint",
            summary = toRetrievalText(),
            sourceUri = null,
            retrievalWeight = 1f,
        )

    private fun MemoryShard.toRetrievalText(): String =
        listOf(title, summary, type.name).joinToString(separator = "\n")
}

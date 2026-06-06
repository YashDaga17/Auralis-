package com.brandforge.app.data.memory

import com.brandforge.app.data.memory.qdrant.QdrantApi
import com.brandforge.app.data.memory.qdrant.QdrantCondition
import com.brandforge.app.data.memory.qdrant.QdrantCreateCollectionRequest
import com.brandforge.app.data.memory.qdrant.QdrantFilter
import com.brandforge.app.data.memory.qdrant.QdrantMatch
import com.brandforge.app.data.memory.qdrant.QdrantPoint
import com.brandforge.app.data.memory.qdrant.QdrantSearchRequest
import com.brandforge.app.data.memory.qdrant.QdrantUpsertPointsRequest
import com.brandforge.app.data.memory.qdrant.QdrantVectorConfig
import com.brandforge.app.domain.memory.EmbeddingVector
import com.brandforge.app.domain.memory.MemoryShard
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QdrantMemoryRemoteDataSource @Inject constructor(
    private val api: QdrantApi,
) {
    suspend fun upsert(memory: MemoryShard, embedding: EmbeddingVector): String {
        require(embedding.values.isNotEmpty()) { "Qdrant upsert requires a non-empty embedding vector" }
        ensureCollection(embedding.values.size)
        api.upsertPoints(
            collectionName = CollectionName,
            request = QdrantUpsertPointsRequest(
                points = listOf(
                    QdrantPoint(
                        id = memory.id,
                        vector = embedding.values,
                        payload = mapOf(
                            "creator_id" to memory.creatorId,
                            "type" to memory.type.name,
                            "title" to memory.title,
                            "summary" to memory.summary,
                            "source_uri" to memory.sourceUri,
                            "updated_at" to memory.updatedAt,
                        ),
                    ),
                ),
            ),
        )
        return memory.id
    }

    suspend fun search(
        creatorId: String,
        embedding: EmbeddingVector,
        limit: Int,
    ): Map<String, Float> {
        require(embedding.values.isNotEmpty()) { "Qdrant search requires a non-empty embedding vector" }
        val response = api.searchPoints(
            collectionName = CollectionName,
            request = QdrantSearchRequest(
                vector = embedding.values,
                limit = limit.coerceAtLeast(1),
                filter = QdrantFilter(
                    must = listOf(
                        QdrantCondition(
                            key = "creator_id",
                            match = QdrantMatch(value = creatorId),
                        ),
                    ),
                ),
            ),
        )
        return response.result.orEmpty().associate { point -> point.id to point.score }
    }

    private suspend fun ensureCollection(vectorSize: Int) {
        runCatching {
            api.createCollection(
                collectionName = CollectionName,
                request = QdrantCreateCollectionRequest(
                    vectors = QdrantVectorConfig(size = vectorSize),
                ),
            )
        }
    }

    private companion object {
        const val CollectionName = "brandforge_creator_memory"
    }
}

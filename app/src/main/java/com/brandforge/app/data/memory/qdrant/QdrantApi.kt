package com.brandforge.app.data.memory.qdrant

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface QdrantApi {
    @PUT("collections/{collectionName}")
    suspend fun createCollection(
        @Path("collectionName") collectionName: String,
        @Body request: QdrantCreateCollectionRequest,
    ): QdrantResponse<Unit>

    @PUT("collections/{collectionName}/points")
    suspend fun upsertPoints(
        @Path("collectionName") collectionName: String,
        @Query("wait") wait: Boolean = true,
        @Body request: QdrantUpsertPointsRequest,
    ): QdrantResponse<QdrantOperationResult>

    @POST("collections/{collectionName}/points/search")
    suspend fun searchPoints(
        @Path("collectionName") collectionName: String,
        @Body request: QdrantSearchRequest,
    ): QdrantResponse<List<QdrantScoredPoint>>
}

data class QdrantCreateCollectionRequest(
    val vectors: QdrantVectorConfig,
)

data class QdrantVectorConfig(
    val size: Int,
    val distance: String = "Cosine",
)

data class QdrantUpsertPointsRequest(
    val points: List<QdrantPoint>,
)

data class QdrantPoint(
    val id: String,
    val vector: List<Float>,
    val payload: Map<String, Any?>,
)

data class QdrantSearchRequest(
    val vector: List<Float>,
    val limit: Int,
    val filter: QdrantFilter?,
    val with_payload: Boolean = true,
)

data class QdrantFilter(
    val must: List<QdrantCondition>,
)

data class QdrantCondition(
    val key: String,
    val match: QdrantMatch,
)

data class QdrantMatch(
    val value: String,
)

data class QdrantScoredPoint(
    val id: String,
    val score: Float,
    val payload: Map<String, Any?>? = null,
)

data class QdrantOperationResult(
    val status: String?,
)

data class QdrantResponse<T>(
    val result: T?,
    val status: String?,
    val time: Double?,
)

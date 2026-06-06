package com.brandforge.app.core.ai.gemini

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiVideoApi {
    @POST("v1beta/{model}:predictLongRunning")
    suspend fun predictLongRunning(
        @Path(value = "model", encoded = true) model: String,
        @Body request: JsonObject,
    ): JsonObject

    @GET("v1beta/{operationName}")
    suspend fun getOperation(
        @Path(value = "operationName", encoded = true) operationName: String,
    ): JsonObject
}

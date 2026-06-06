package com.brandforge.app.domain.content

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.brandforge.app.core.ai.gemini.GeminiGenerationApi
import com.brandforge.app.core.ai.gemini.GeminiGenerationConfig
import com.brandforge.app.core.ai.gemini.GeminiGenerationContent
import com.brandforge.app.core.ai.gemini.GeminiGenerationPart
import com.brandforge.app.core.ai.gemini.GeminiGenerationRequest
import com.brandforge.app.core.ai.gemini.GeminiVideoApi
import com.brandforge.app.core.network.GeminiOkHttp
import com.brandforge.app.core.network.IoDispatcher
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryQuery
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class MediaGenerationAgent @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiGenerationApi: GeminiGenerationApi,
    private val geminiVideoApi: GeminiVideoApi,
    private val creatorMemoryRepository: CreatorMemoryRepository,
    private val mediaArtifactRepository: MediaArtifactRepository,
    @GeminiOkHttp private val geminiOkHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun generateImage(request: MediaGenerationRequest): ContentMediaArtifact =
        withContext(ioDispatcher) {
            val prompt = assembleMediaPrompt(
                creatorId = request.creatorId,
                prompt = request.prompt,
                mediaType = MediaArtifactType.Image,
            )
            val generated = generateImageBytes(prompt)
            val id = "image-${request.creatorId}-${System.currentTimeMillis()}"
            val file = saveBytes(
                id = id,
                bytes = generated.bytes,
                extension = generated.mimeType.extension(default = "png"),
            )
            mediaArtifactRepository.persist(
                ContentMediaArtifactInput(
                    id = id,
                    creatorId = request.creatorId,
                    type = MediaArtifactType.Image,
                    prompt = prompt,
                    localUri = Uri.fromFile(file).toString(),
                    remoteUri = null,
                    mimeType = generated.mimeType,
                    model = generated.model,
                    status = MediaArtifactStatus.Ready,
                    errorMessage = null,
                    sourceDraftId = request.sourceDraftId,
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }

    suspend fun generateVideo(request: MediaGenerationRequest): ContentMediaArtifact =
        withContext(ioDispatcher) {
            val prompt = assembleMediaPrompt(
                creatorId = request.creatorId,
                prompt = request.prompt,
                mediaType = MediaArtifactType.Video,
            )
            val generated = generateVideoAsset(prompt)
            val id = "video-${request.creatorId}-${System.currentTimeMillis()}"
            val localFile = generated.bytes?.let { bytes ->
                saveBytes(
                    id = id,
                    bytes = bytes,
                    extension = generated.mimeType.extension(default = "mp4"),
                )
            } ?: generated.remoteUri?.let { uri ->
                downloadRemoteVideo(id, uri, generated.mimeType)
            }
            mediaArtifactRepository.persist(
                ContentMediaArtifactInput(
                    id = id,
                    creatorId = request.creatorId,
                    type = MediaArtifactType.Video,
                    prompt = prompt,
                    localUri = localFile?.let { Uri.fromFile(it).toString() },
                    remoteUri = generated.remoteUri,
                    mimeType = generated.mimeType,
                    model = generated.model,
                    status = if (localFile != null) MediaArtifactStatus.Ready else MediaArtifactStatus.RemoteReady,
                    errorMessage = null,
                    sourceDraftId = request.sourceDraftId,
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }

    private suspend fun assembleMediaPrompt(
        creatorId: String,
        prompt: String,
        mediaType: MediaArtifactType,
    ): String {
        val brandDna = withTimeoutOrNull(1_500L) {
            creatorMemoryRepository.observeBrandDna(creatorId).first()
        } ?: error("Brand DNA is required before media generation")
        val memories = creatorMemoryRepository.retrieve(
            MemoryQuery(
                creatorId = creatorId,
                query = "visual style brand voice audience hooks banned claims content goals $prompt",
                limit = 6,
            ),
        )
        return """
            Create a ${mediaType.label.lowercase()} for this creator.

            CREATOR
            Name: ${brandDna.creatorName}
            Archetype: ${brandDna.archetype}
            Voice rules: ${brandDna.voiceRulesJson}
            Banned claims: ${brandDna.bannedClaimsJson}
            Goals: ${brandDna.businessGoalsJson}

            MEMORY
            ${memories.joinToString("\n") { "- ${it.type.name}: ${it.title} :: ${it.summary}" }}

            USER MEDIA PROMPT
            $prompt

            REQUIREMENTS
            - Make it creator-specific and aligned to Brand DNA.
            - Avoid unsafe, vulgar, copyrighted, hateful, or unsupported claims.
            - Use a mobile-first social-media composition.
            - For video, create a vertical short-form asset suitable for Reels/Shorts.
            ${if (mediaType == MediaArtifactType.Image) "- Return an actual rendered image as inline image data. Do not return only a written description." else ""}
            ${if (mediaType == MediaArtifactType.Video) "- Return a playable vertical video asset or a provider file URI when generation finishes." else ""}
        """.trimIndent()
    }

    private suspend fun generateImageBytes(prompt: String): GeneratedMediaBytes {
        val errors = mutableListOf<String>()
        ImageModels.forEach { model ->
            val response = runCatching {
                geminiGenerationApi.generateContent(
                    model = model,
                    request = GeminiGenerationRequest(
                        contents = listOf(
                            GeminiGenerationContent(
                                parts = listOf(GeminiGenerationPart(text = prompt)),
                            ),
                        ),
                        generationConfig = GeminiGenerationConfig(
                            temperature = 0.72,
                            maxOutputTokens = 1_024,
                            responseModalities = listOf("TEXT", "IMAGE"),
                        ),
                    ),
                )
            }.onFailure { errors += "$model: ${it.message}" }.getOrNull()
            val inlineData = response?.candidates
                .orEmpty()
                .flatMap { it.content?.parts.orEmpty() }
                .firstNotNullOfOrNull { it.inlineData }
            if (inlineData != null && inlineData.data.isNotBlank()) {
                val bytes = runCatching {
                    Base64.decode(inlineData.data, Base64.DEFAULT)
                }.onFailure { errors += "$model returned image data that could not be decoded: ${it.message}" }
                    .getOrNull()
                if (bytes != null && bytes.isNotEmpty()) {
                    return GeneratedMediaBytes(
                        bytes = bytes,
                        mimeType = inlineData.mimeType.ifBlank { "image/png" },
                        model = model,
                    )
                }
            }
            val textOutput = response?.candidates
                .orEmpty()
                .flatMap { it.content?.parts.orEmpty() }
                .mapNotNull { it.text?.trim()?.takeIf(String::isNotBlank) }
                .joinToString(separator = "\n")
                .take(360)
            if (textOutput.isNotBlank()) {
                errors += "$model returned text instead of image bytes: $textOutput"
            } else if (response != null) {
                errors += "$model returned no inline image bytes"
            }
        }
        error("Image generation did not return rendered image bytes. ${errors.joinToString(" | ")}")
    }

    private suspend fun generateVideoAsset(prompt: String): GeneratedVideoAsset {
        val errors = mutableListOf<String>()
        VideoModels.forEach { model ->
            val operation = runCatching {
                geminiVideoApi.predictLongRunning(
                    model = model,
                    request = videoRequest(prompt),
                )
            }.onFailure { errors += "$model start: ${it.message}" }.getOrNull() ?: return@forEach

            val operationName = operation.getString("name")
            if (operationName.isBlank()) {
                errors += "$model did not return an operation name"
                return@forEach
            }

            repeat(VideoPollAttempts) {
                delay(VideoPollDelayMillis)
                val latest = runCatching {
                    geminiVideoApi.getOperation(operationName)
                }.onFailure { errors += "$model poll: ${it.message}" }.getOrNull() ?: return@repeat

                latest.obj("error")?.let { providerError ->
                    errors += "$model error: $providerError"
                    return@forEach
                }
                if (latest.bool("done")) {
                    val response = latest.obj("response") ?: latest
                    val base64 = response.findFirstString(setOf("bytesBase64Encoded", "bytesBase64", "data"))
                    val uri = response.findFirstUri()
                    if (!base64.isNullOrBlank()) {
                        return GeneratedVideoAsset(
                            bytes = Base64.decode(base64, Base64.DEFAULT),
                            remoteUri = uri,
                            mimeType = response.findMimeType() ?: "video/mp4",
                            model = model,
                        )
                    }
                    if (!uri.isNullOrBlank()) {
                        return GeneratedVideoAsset(
                            bytes = null,
                            remoteUri = uri,
                            mimeType = response.findMimeType() ?: "video/mp4",
                            model = model,
                        )
                    }
                    errors += "$model finished but returned no video bytes or URI"
                    return@forEach
                }
            }
            errors += "$model operation did not finish within ${VideoPollAttempts * VideoPollDelayMillis / 1_000}s"
        }
        error("Video generation failed or timed out. ${errors.joinToString(" | ")}")
    }

    private fun videoRequest(prompt: String): JsonObject =
        JsonObject().apply {
            add(
                "instances",
                JsonArray().apply {
                    add(
                        JsonObject().apply {
                            addProperty("prompt", prompt)
                        },
                    )
                },
            )
            add(
                "parameters",
                JsonObject().apply {
                    addProperty("aspectRatio", "9:16")
                    addProperty("durationSeconds", 8)
                    addProperty("personGeneration", "allow_adult")
                },
            )
        }

    private fun saveBytes(id: String, bytes: ByteArray, extension: String): File {
        val directory = File(context.filesDir, "generated-media").apply { mkdirs() }
        return File(directory, "$id.$extension").also { file ->
            file.writeBytes(bytes)
        }
    }

    private fun downloadRemoteVideo(id: String, remoteUri: String, mimeType: String): File? =
        runCatching {
            val response = geminiOkHttpClient.newCall(
                Request.Builder()
                    .url(remoteUri)
                    .build(),
            ).execute()
            if (!response.isSuccessful) return null
            val bytes = response.body?.bytes() ?: return null
            saveBytes(
                id = id,
                bytes = bytes,
                extension = mimeType.extension(default = "mp4"),
            )
        }.getOrNull()

    private fun JsonObject.getString(name: String): String =
        get(name)?.takeIf { it.isJsonPrimitive }?.asString.orEmpty()

    private fun JsonObject.bool(name: String): Boolean =
        get(name)?.takeIf { it.isJsonPrimitive }?.asBoolean == true

    private fun JsonObject.obj(name: String): JsonObject? =
        get(name)?.takeIf { it.isJsonObject }?.asJsonObject

    private fun JsonElement.findFirstString(names: Set<String>): String? {
        if (isJsonObject) {
            val obj = asJsonObject
            names.forEach { name ->
                obj.get(name)?.takeIf { it.isJsonPrimitive }?.asString?.let { return it }
            }
            obj.entrySet().forEach { entry ->
                entry.value.findFirstString(names)?.let { return it }
            }
        }
        if (isJsonArray) {
            asJsonArray.forEach { child ->
                child.findFirstString(names)?.let { return it }
            }
        }
        return null
    }

    private fun JsonElement.findFirstUri(): String? =
        findFirstString(setOf("uri", "url", "videoUri", "fileUri"))
            ?.takeIf { it.startsWith("http://") || it.startsWith("https://") }

    private fun JsonElement.findMimeType(): String? =
        findFirstString(setOf("mimeType", "mime_type"))

    private fun String.extension(default: String): String =
        substringAfter("/", missingDelimiterValue = default)
            .substringBefore(";")
            .lowercase()
            .let { ext ->
                when (ext) {
                    "jpeg" -> "jpg"
                    "quicktime" -> "mov"
                    "mp4", "mpeg", "png", "jpg", "webp", "gif" -> ext
                    else -> default
                }
            }

    private data class GeneratedMediaBytes(
        val bytes: ByteArray,
        val mimeType: String,
        val model: String,
    )

    private data class GeneratedVideoAsset(
        val bytes: ByteArray?,
        val remoteUri: String?,
        val mimeType: String,
        val model: String,
    )

    private companion object {
        val ImageModels = listOf(
            "models/gemini-2.5-flash-image-preview",
            "models/gemini-2.0-flash-preview-image-generation",
        )
        val VideoModels = listOf(
            "models/veo-3.0-generate-preview",
            "models/veo-2.0-generate-001",
        )
        const val VideoPollAttempts = 12
        const val VideoPollDelayMillis = 5_000L
    }
}

package com.brandforge.app.presentation.pr

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandforge.app.core.ai.gemini.GeminiGenerationApi
import com.brandforge.app.core.ai.gemini.GeminiGenerationConfig
import com.brandforge.app.core.ai.gemini.GeminiGenerationContent
import com.brandforge.app.core.ai.gemini.GeminiInlineData
import com.brandforge.app.core.ai.gemini.GeminiGenerationPart
import com.brandforge.app.core.ai.gemini.GeminiGenerationRequest
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.core.model.MemoryType
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryQuery
import com.brandforge.app.domain.memory.MemoryShardDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
class PrRiskAuditViewModel @Inject constructor(
    private val geminiGenerationApi: GeminiGenerationApi,
    private val creatorMemoryRepository: CreatorMemoryRepository,
    private val preferencesStore: CreatorPreferencesStore,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrRiskAuditUiState())
    val uiState: StateFlow<PrRiskAuditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesStore.selectedCreatorId.collect { creatorId ->
                if (!creatorId.isNullOrBlank() && _uiState.value.creatorId.isBlank()) {
                    updateCreatorId(creatorId)
                }
            }
        }
    }

    fun updateCreatorId(value: String) {
        _uiState.update { it.copy(creatorId = value, errorMessage = null) }
    }

    fun updateMediaUri(value: String) {
        _uiState.update { it.copy(mediaUri = value, errorMessage = null) }
    }

    fun updateMediaContext(value: String) {
        _uiState.update { it.copy(mediaContext = value, errorMessage = null) }
    }

    fun updateCaption(value: String) {
        _uiState.update { it.copy(caption = value, errorMessage = null) }
    }

    fun audit() {
        val state = _uiState.value
        val creatorId = state.creatorId.trim()
        if (creatorId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Creator ID is required") }
            return
        }
        if (state.mediaUri.isBlank() && state.caption.isBlank() && state.mediaContext.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Upload media or enter caption/context") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(auditing = true, errorMessage = null) }
            runCatching {
                val brandDna = withTimeoutOrNull(1_500L) {
                    creatorMemoryRepository.observeBrandDna(creatorId).first()
                }
                val memories = runCatching {
                    creatorMemoryRepository.retrieve(
                        MemoryQuery(
                            creatorId = creatorId,
                            query = "brand safety banned claims PR risk caption audience objections content performance",
                            limit = 8,
                        ),
                    )
                }.getOrDefault(emptyList())
                val report = generateAuditReport(
                    creatorId = creatorId,
                    mediaUri = state.mediaUri,
                    mediaContext = state.mediaContext,
                    caption = state.caption,
                    imagePart = state.mediaUri.takeIf { it.isNotBlank() }?.let(::readInlineImagePart),
                    brandDnaBlock = brandDna?.let {
                        """
                        Creator: ${it.creatorName}
                        Archetype: ${it.archetype}
                        Voice Rules: ${it.voiceRulesJson}
                        Banned Claims: ${it.bannedClaimsJson}
                        Goals: ${it.businessGoalsJson}
                        """.trimIndent()
                    }.orEmpty(),
                    memoryBlock = memories.joinToString("\n") { "- ${it.type.name}: ${it.title} :: ${it.summary}" },
                )
                creatorMemoryRepository.writeMemory(
                    MemoryShardDraft(
                        id = "pr-audit-$creatorId-${System.currentTimeMillis()}",
                        creatorId = creatorId,
                        type = MemoryType.PerformanceHistory,
                        title = "PR audit report",
                        summary = report.take(1_600),
                        sourceUri = state.mediaUri.ifBlank { null },
                        retrievalWeight = 0.74f,
                    ),
                )
                report
            }.onSuccess { report ->
                _uiState.update {
                    it.copy(
                        auditing = false,
                        report = report,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        auditing = false,
                        errorMessage = throwable.message ?: "Unable to audit PR risk",
                    )
                }
            }
        }
    }

    private suspend fun generateAuditReport(
        creatorId: String,
        mediaUri: String,
        mediaContext: String,
        caption: String,
        imagePart: GeminiGenerationPart?,
        brandDnaBlock: String,
        memoryBlock: String,
    ): String {
        val prompt = """
            You are BrandForge PR Risk Agent.
            Audit this creator asset before posting.
            If an image part is attached, inspect the actual visual composition, readable text, subject, objects, emotional tone, safety issues, brand fit, and caption alignment.
            If no image part is attached or the upload is a video/document, use the uploaded URI, creator-provided media context, caption, Brand DNA, banned claims, and memory.

            CREATOR ID
            $creatorId

            UPLOADED MEDIA URI
            ${mediaUri.ifBlank { "none" }}

            MEDIA ANALYSIS MODE
            ${if (imagePart != null) "Gemini multimodal image audit attached." else "Text/context audit only. Ask for image upload or clearer media context if visual evidence is insufficient."}

            MEDIA CONTEXT / DESCRIPTION
            ${mediaContext.ifBlank { "none provided" }}

            CAPTION
            ${caption.ifBlank { "none provided" }}

            BRAND DNA
            ${brandDnaBlock.ifBlank { "Brand DNA unavailable. Ask creator to save DNA first." }}

            MEMORY
            ${memoryBlock.ifBlank { "No memory retrieved." }}

            Return plain text using exactly these section labels. Do not use markdown tables.

            OVERALL_RISK:
            LOW, MEDIUM, or HIGH with one sentence.

            WHAT_IS_NOT_WORKING:
            Explain what in the content, media context, or caption may fail and why.

            CAPTION_RECOMMENDATION:
            Explain the caption strategy BrandForge recommends for this creator.

            REVISED_CAPTION:
            Write a ready-to-post caption. Always include this section even if the original caption is empty.

            WHY_THIS_CAPTION_WORKS:
            Explain how it fits Brand DNA, memory, and audience expectations.

            CLAIMS_OR_TONE_RISKS:
            Mention banned claims, vulgarity, unsafe promises, or tone mismatch.

            AUDIENCE_RISK:
            Mention how the audience may misread or reject it.

            FINAL_DECISION:
            PUBLISH, REVISE, or DO_NOT_PUBLISH.

            FIXES_BEFORE_POSTING:
            Give exact edits the creator should make before posting.
        """.trimIndent()
        return geminiGenerationApi.generateContent(
            model = "models/gemini-2.5-flash",
            request = GeminiGenerationRequest(
                contents = listOf(
                    GeminiGenerationContent(
                        parts = listOfNotNull(GeminiGenerationPart(prompt), imagePart),
                    ),
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.35,
                    maxOutputTokens = 1_800,
                ),
            ),
        ).candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.joinToString("\n") { it.text.orEmpty() }
            .orEmpty()
            .ifBlank { error("Gemini returned an empty PR audit report") }
    }

    private fun readInlineImagePart(mediaUri: String): GeminiGenerationPart? {
        val uri = Uri.parse(mediaUri)
        val mimeType = appContext.contentResolver.getType(uri) ?: mediaUri.guessMimeType()
        if (!mimeType.startsWith("image/")) return null
        val bytes = appContext.contentResolver.openInputStream(uri)?.use { input ->
            ByteArrayOutputStream().use { output ->
                input.copyTo(output, bufferSize = 16 * 1024)
                output.toByteArray()
            }
        } ?: ByteArray(0)
        if (bytes.isEmpty()) return null
        require(bytes.size <= MaxInlineImageBytes) {
            "Image is too large for inline Gemini audit. Pick a smaller image or screenshot under 7 MB."
        }
        return GeminiGenerationPart(
            inlineData = GeminiInlineData(
                mimeType = mimeType,
                data = Base64.encodeToString(bytes, Base64.NO_WRAP),
            ),
        )
    }

    private fun String.guessMimeType(): String =
        when {
            endsWith(".png", ignoreCase = true) -> "image/png"
            endsWith(".webp", ignoreCase = true) -> "image/webp"
            endsWith(".gif", ignoreCase = true) -> "image/gif"
            else -> "image/jpeg"
        }

    private companion object {
        const val MaxInlineImageBytes = 7 * 1024 * 1024
    }
}

package com.brandforge.app.presentation.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.domain.content.ContentAgent
import com.brandforge.app.domain.content.ContentFormat
import com.brandforge.app.domain.content.ContentMediaArtifactInput
import com.brandforge.app.domain.content.ContentRepository
import com.brandforge.app.domain.content.MediaArtifactRepository
import com.brandforge.app.domain.content.MediaArtifactStatus
import com.brandforge.app.domain.content.MediaArtifactType
import com.brandforge.app.domain.content.MediaGenerationAgent
import com.brandforge.app.domain.content.MediaGenerationRequest
import com.brandforge.app.domain.trend.TrendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentStudioViewModel @Inject constructor(
    private val trendRepository: TrendRepository,
    private val contentRepository: ContentRepository,
    private val contentAgent: ContentAgent,
    private val preferencesStore: CreatorPreferencesStore,
    private val mediaArtifactRepository: MediaArtifactRepository,
    private val mediaGenerationAgent: MediaGenerationAgent,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContentStudioUiState())
    val uiState: StateFlow<ContentStudioUiState> = _uiState.asStateFlow()

    private var opportunitiesJob: Job? = null
    private var draftsJob: Job? = null
    private var mediaJob: Job? = null

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
        val creatorId = value.trim()
        _uiState.update { it.copy(creatorId = value, errorMessage = null) }
        observeCreatorContent(creatorId)
    }

    fun generate(trendId: String, format: ContentFormat) {
        val trend = _uiState.value.opportunities.firstOrNull { it.id == trendId }
        if (trend == null) {
            _uiState.update { it.copy(errorMessage = "Trend opportunity not found") }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    generatingTrendId = trendId,
                    generatingFormat = format,
                    errorMessage = null,
                )
            }
            runCatching {
                contentAgent.generate(trend = trend, format = format)
            }.onSuccess { draft ->
                _uiState.update {
                    it.copy(
                        generatingTrendId = null,
                        generatingFormat = null,
                        drafts = listOf(draft) + it.drafts.filterNot { existing -> existing.id == draft.id },
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        generatingTrendId = null,
                        generatingFormat = null,
                        errorMessage = throwable.message ?: "Unable to generate content",
                    )
                }
            }
        }
    }

    fun updateMediaPrompt(value: String) {
        _uiState.update { it.copy(mediaPrompt = value, errorMessage = null) }
    }

    fun generateMedia(type: MediaArtifactType) {
        val state = _uiState.value
        val creatorId = state.creatorId.trim()
        val sourceDraft = state.drafts.firstOrNull()
        val prompt = state.mediaPrompt.trim().ifBlank {
            sourceDraft?.let { draft ->
                "${draft.title}\n\n${draft.content.take(1_400)}"
            }.orEmpty()
        }
        if (creatorId.isBlank() || prompt.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Creator ID and media prompt are required") }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    generatingMediaType = type,
                    errorMessage = null,
                )
            }
            val request = MediaGenerationRequest(
                creatorId = creatorId,
                prompt = prompt,
                sourceDraftId = sourceDraft?.id,
            )
            runCatching {
                when (type) {
                    MediaArtifactType.Image -> mediaGenerationAgent.generateImage(request)
                    MediaArtifactType.Video -> mediaGenerationAgent.generateVideo(request)
                }
            }.onSuccess { artifact ->
                _uiState.update {
                    it.copy(
                        generatingMediaType = null,
                        mediaArtifacts = listOf(artifact) + it.mediaArtifacts.filterNot { existing -> existing.id == artifact.id },
                    )
                }
            }.onFailure { throwable ->
                val failed = mediaArtifactRepository.persist(
                    ContentMediaArtifactInput(
                        id = "${type.name.lowercase()}-failed-$creatorId-${System.currentTimeMillis()}",
                        creatorId = creatorId,
                        type = type,
                        prompt = prompt,
                        localUri = null,
                        remoteUri = null,
                        mimeType = when (type) {
                            MediaArtifactType.Image -> "image/png"
                            MediaArtifactType.Video -> "video/mp4"
                        },
                        model = when (type) {
                            MediaArtifactType.Image -> "Gemini image generation"
                            MediaArtifactType.Video -> "Gemini/Veo video generation"
                        },
                        status = MediaArtifactStatus.Failed,
                        errorMessage = throwable.message ?: "Unable to generate media",
                        sourceDraftId = sourceDraft?.id,
                        createdAt = System.currentTimeMillis(),
                    ),
                )
                _uiState.update {
                    it.copy(
                        generatingMediaType = null,
                        mediaArtifacts = listOf(failed) + it.mediaArtifacts.filterNot { existing -> existing.id == failed.id },
                        errorMessage = throwable.message ?: "Unable to generate media",
                    )
                }
            }
        }
    }

    private fun observeCreatorContent(creatorId: String) {
        if (creatorId.isBlank()) return

        opportunitiesJob?.cancel()
        opportunitiesJob = viewModelScope.launch {
            trendRepository.observeOpportunities(creatorId, limit = 12).collect { opportunities ->
                _uiState.update { it.copy(opportunities = opportunities) }
            }
        }

        draftsJob?.cancel()
        draftsJob = viewModelScope.launch {
            contentRepository.observeDrafts(creatorId, limit = 30).collect { drafts ->
                _uiState.update { it.copy(drafts = drafts) }
            }
        }

        mediaJob?.cancel()
        mediaJob = viewModelScope.launch {
            mediaArtifactRepository.observeArtifacts(creatorId, limit = 30).collect { artifacts ->
                _uiState.update { it.copy(mediaArtifacts = artifacts) }
            }
        }
    }
}

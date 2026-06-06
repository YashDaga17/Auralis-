package com.brandforge.app.presentation.lead

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.domain.lead.AudienceInteractionType
import com.brandforge.app.domain.lead.LeadDetectionAgent
import com.brandforge.app.domain.lead.LeadInteractionInput
import com.brandforge.app.domain.lead.LeadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeadInboxViewModel @Inject constructor(
    private val leadDetectionAgent: LeadDetectionAgent,
    private val leadRepository: LeadRepository,
    private val preferencesStore: CreatorPreferencesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LeadInboxUiState())
    val uiState: StateFlow<LeadInboxUiState> = _uiState.asStateFlow()

    private var inboxJob: Job? = null

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
        observeInbox(creatorId)
    }

    fun updateSourceType(value: AudienceInteractionType) {
        _uiState.update { it.copy(sourceType = value, errorMessage = null) }
    }

    fun updatePlatform(value: String) {
        _uiState.update { it.copy(platform = value, errorMessage = null) }
    }

    fun updateAuthorHandle(value: String) {
        _uiState.update { it.copy(authorHandle = value, errorMessage = null) }
    }

    fun updateInteractionText(value: String) {
        _uiState.update { it.copy(interactionText = value, errorMessage = null) }
    }

    fun classify() {
        val state = _uiState.value
        val creatorId = state.creatorId.trim()
        val text = state.interactionText.trim()
        if (creatorId.isBlank() || text.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Creator ID and interaction text are required")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    classifying = true,
                    errorMessage = null,
                )
            }
            runCatching {
                observeInbox(creatorId)
                leadDetectionAgent.classify(
                    LeadInteractionInput(
                        creatorId = creatorId,
                        sourceType = state.sourceType,
                        platform = state.platform,
                        authorHandle = state.authorHandle,
                        text = text,
                    ),
                )
            }.onSuccess { lead ->
                _uiState.update {
                    it.copy(
                        classifying = false,
                        interactionText = "",
                        leads = listOf(lead) + it.leads.filterNot { existing -> existing.id == lead.id },
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        classifying = false,
                        errorMessage = throwable.message ?: "Unable to classify interaction",
                    )
                }
            }
        }
    }

    private fun observeInbox(creatorId: String) {
        if (creatorId.isBlank()) return
        inboxJob?.cancel()
        inboxJob = viewModelScope.launch {
            leadRepository.observeInbox(creatorId).collect { leads ->
                _uiState.update { it.copy(leads = leads) }
            }
        }
    }
}

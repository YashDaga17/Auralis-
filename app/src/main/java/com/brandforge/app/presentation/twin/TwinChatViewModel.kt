package com.brandforge.app.presentation.twin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.domain.twin.TwinChatAgent
import com.brandforge.app.domain.twin.TwinChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TwinChatViewModel @Inject constructor(
    private val twinChatAgent: TwinChatAgent,
    private val twinChatRepository: TwinChatRepository,
    private val preferencesStore: CreatorPreferencesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TwinChatUiState())
    val uiState: StateFlow<TwinChatUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

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
        observeMessages(creatorId)
    }

    fun updateDraftMessage(value: String) {
        _uiState.update { it.copy(draftMessage = value, errorMessage = null) }
    }

    fun send() {
        val state = _uiState.value
        val creatorId = state.creatorId.trim()
        val message = state.draftMessage.trim()
        if (creatorId.isBlank() || message.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Creator ID and message are required")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(sending = true, errorMessage = null, draftMessage = "") }
            runCatching {
                observeMessages(creatorId)
                twinChatAgent.sendMessage(
                    creatorId = creatorId,
                    message = message,
                )
            }.onSuccess {
                _uiState.update { it.copy(sending = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        sending = false,
                        draftMessage = message,
                        errorMessage = throwable.message ?: "Unable to ask Digital Twin",
                    )
                }
            }
        }
    }

    private fun observeMessages(creatorId: String) {
        if (creatorId.isBlank()) return
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            twinChatRepository.observeMessages(creatorId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }
}

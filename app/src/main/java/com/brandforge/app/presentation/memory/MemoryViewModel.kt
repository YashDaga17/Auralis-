package com.brandforge.app.presentation.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.domain.memory.MemoryQuery
import com.brandforge.app.domain.memory.RetrieveCreatorMemoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val retrieveCreatorMemory: RetrieveCreatorMemoryUseCase,
    private val preferencesStore: CreatorPreferencesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

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

    fun updateQuery(value: String) {
        _uiState.update { it.copy(query = value, errorMessage = null) }
    }

    fun retrieve() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching {
                retrieveCreatorMemory(
                    MemoryQuery(
                        creatorId = state.creatorId.trim(),
                        query = state.query.trim().ifBlank { "brand voice creator memory audience goals" },
                        limit = 12,
                    ),
                )
            }.onSuccess { memories ->
                _uiState.update { it.copy(loading = false, results = memories) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = throwable.message ?: "Unable to retrieve creator memory",
                    )
                }
            }
        }
    }
}

package com.brandforge.app.presentation.trend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.core.model.MemoryType
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryShardDraft
import com.brandforge.app.domain.trend.TrendAgent
import com.brandforge.app.domain.trend.TrendOpportunity
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
class TrendRadarViewModel @Inject constructor(
    private val trendAgent: TrendAgent,
    private val trendRepository: TrendRepository,
    private val creatorMemoryRepository: CreatorMemoryRepository,
    private val preferencesStore: CreatorPreferencesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrendRadarUiState())
    val uiState: StateFlow<TrendRadarUiState> = _uiState.asStateFlow()
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
        _uiState.update { it.copy(creatorId = value, errorMessage = null, saveMessage = null) }
        observeOpportunities(value.trim())
    }

    fun scan() {
        val creatorId = _uiState.value.creatorId.trim()
        viewModelScope.launch {
            _uiState.update { it.copy(scanning = true, errorMessage = null) }
            runCatching {
                observeOpportunities(creatorId)
                trendAgent.scan(creatorId)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        scanning = false,
                        signalCount = result.signals.size,
                        opportunities = result.opportunities,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        scanning = false,
                        errorMessage = throwable.message ?: "Unable to scan trend radar",
                    )
                }
            }
        }
    }

    fun saveOpportunity(opportunityId: String) {
        val opportunity = _uiState.value.opportunities.firstOrNull { it.id == opportunityId } ?: return
        viewModelScope.launch {
            runCatching {
                creatorMemoryRepository.writeMemory(opportunity.toMemoryDraft())
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        savedOpportunityIds = state.savedOpportunityIds + opportunityId,
                        saveMessage = "Saved to Trend History",
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        errorMessage = throwable.message ?: "Unable to save trend opportunity",
                        saveMessage = null,
                    )
                }
            }
        }
    }

    private fun observeOpportunities(creatorId: String) {
        if (creatorId.isBlank()) return
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            trendRepository.observeOpportunities(creatorId).collect { opportunities ->
                _uiState.update { it.copy(opportunities = opportunities) }
            }
        }
    }

    private fun TrendOpportunity.toMemoryDraft(): MemoryShardDraft =
        MemoryShardDraft(
            id = "saved-trend-${creatorId}-${id}",
            creatorId = creatorId,
            type = MemoryType.TrendHistory,
            title = "Saved trend opportunity: $title",
            summary = """
                Platform: $sourcePlatform
                Recommended format: $recommendedFormat
                Opportunity: ${(opportunityScore * 100).toInt()}%
                Brand fit: ${(brandFitScore * 100).toInt()}%
                Rationale: $rationale
                Summary: $summary
            """.trimIndent(),
            sourceUri = sourceUrl.ifBlank { null },
            retrievalWeight = (0.66f + (opportunityScore * 0.3f)).coerceIn(0.55f, 0.96f),
        )
}

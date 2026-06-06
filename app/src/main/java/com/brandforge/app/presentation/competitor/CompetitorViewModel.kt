package com.brandforge.app.presentation.competitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.domain.competitor.CompetitorAgent
import com.brandforge.app.domain.competitor.CompetitorAnalysisRequest
import com.brandforge.app.domain.competitor.CompetitorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetitorViewModel @Inject constructor(
    private val competitorAgent: CompetitorAgent,
    private val competitorRepository: CompetitorRepository,
    private val preferencesStore: CreatorPreferencesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompetitorUiState())
    val uiState: StateFlow<CompetitorUiState> = _uiState.asStateFlow()

    private var competitorsJob: Job? = null
    private var insightsJob: Job? = null

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
        observeCompetitorIntel(creatorId)
        refreshContentSnapshot(creatorId)
    }

    fun updateCompetitorUrl(value: String) {
        _uiState.update { it.copy(competitorUrl = value, errorMessage = null) }
    }

    fun analyze() {
        val state = _uiState.value
        val creatorId = state.creatorId.trim()
        val url = state.competitorUrl.trim()
        if (creatorId.isBlank() || url.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Creator ID and competitor URL are required")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(analyzing = true, errorMessage = null) }
            runCatching {
                observeCompetitorIntel(creatorId)
                competitorAgent.analyze(
                    CompetitorAnalysisRequest(
                        creatorId = creatorId,
                        url = url,
                    ),
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        analyzing = false,
                        competitorUrl = "",
                        competitors = listOf(result.competitor) + it.competitors.filterNot { existing -> existing.id == result.competitor.id },
                        content = result.content + it.content.filterNot { existing -> result.content.any { content -> content.id == existing.id } },
                        insights = result.insights + it.insights.filterNot { existing -> result.insights.any { insight -> insight.id == existing.id } },
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        analyzing = false,
                        errorMessage = throwable.message ?: "Unable to analyze competitor",
                    )
                }
            }
        }
    }

    private fun observeCompetitorIntel(creatorId: String) {
        if (creatorId.isBlank()) return

        competitorsJob?.cancel()
        competitorsJob = viewModelScope.launch {
            competitorRepository.observeCompetitors(creatorId).collect { competitors ->
                _uiState.update { it.copy(competitors = competitors) }
            }
        }

        insightsJob?.cancel()
        insightsJob = viewModelScope.launch {
            competitorRepository.observeInsights(creatorId).collect { insights ->
                _uiState.update { it.copy(insights = insights) }
            }
        }
    }

    private fun refreshContentSnapshot(creatorId: String) {
        if (creatorId.isBlank()) return
        viewModelScope.launch {
            runCatching {
                competitorRepository.latestContent(creatorId, limit = 20)
            }.onSuccess { content ->
                _uiState.update { it.copy(content = content) }
            }
        }
    }
}

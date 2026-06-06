package com.brandforge.app.presentation.warroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandforge.app.core.ai.openrouter.OpenRouterChatRequest
import com.brandforge.app.core.ai.openrouter.OpenRouterClient
import com.brandforge.app.core.ai.openrouter.OpenRouterMessage
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.domain.competitor.CompetitorRepository
import com.brandforge.app.domain.content.ContentRepository
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryQuery
import com.brandforge.app.domain.trend.TrendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
class WarRoomViewModel @Inject constructor(
    private val openRouterClient: OpenRouterClient,
    private val preferencesStore: CreatorPreferencesStore,
    private val creatorMemoryRepository: CreatorMemoryRepository,
    private val trendRepository: TrendRepository,
    private val contentRepository: ContentRepository,
    private val competitorRepository: CompetitorRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WarRoomUiState())
    val uiState: StateFlow<WarRoomUiState> = _uiState.asStateFlow()

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

    fun updateBrief(value: String) {
        _uiState.update { it.copy(brief = value, errorMessage = null) }
    }

    fun runCaptionBattle() {
        val state = _uiState.value
        val creatorId = state.creatorId.trim()
        val brief = state.brief.trim()
        if (creatorId.isBlank() || brief.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Creator ID and battle brief are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(runningBattle = true, errorMessage = null) }
            runCatching {
                val context = assembleContext(creatorId)
                val response = openRouterClient.createChatCompletion(
                    OpenRouterChatRequest(
                        model = "minimax/minimax-01",
                        messages = listOf(
                            OpenRouterMessage(
                                role = "system",
                                content = "You are BrandForge War Room, a panel of autonomous creator agents. Be sharp, practical, and creator-specific.",
                            ),
                            OpenRouterMessage(
                                role = "user",
                                content = """
                                    Run a caption battle for this creator.

                                    CREATOR CONTEXT
                                    $context

                                    BATTLE BRIEF
                                    $brief

                                    Simulate:
                                    - Brand DNA Agent: protects voice and claims
                                    - Virality Agent: maximizes hook and retention
                                    - Competitor Agent: differentiates from market
                                    - Supervisor Agent: chooses final answer

                                    Return:
                                    1. Agent debate, one short paragraph per agent
                                    2. Three caption candidates
                                    3. Score table: brand fit / virality / clarity / risk
                                    4. Winning caption
                                    5. Why it wins
                                    6. One safer alternative if PR risk is medium/high
                                """.trimIndent(),
                            ),
                        ),
                        temperature = 0.78,
                        maxTokens = 1_700,
                    ),
                )
                response.choices.firstOrNull()?.message?.content.orEmpty()
                    .ifBlank { error("OpenRouter returned an empty War Room result") }
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        runningBattle = false,
                        battleResult = result,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        runningBattle = false,
                        errorMessage = throwable.message ?: "Unable to run War Room battle",
                    )
                }
            }
        }
    }

    private suspend fun assembleContext(creatorId: String): String {
        val brandDna = withTimeoutOrNull(1_500L) {
            creatorMemoryRepository.observeBrandDna(creatorId).first()
        }
        val memories = runCatching {
            creatorMemoryRepository.retrieve(
                MemoryQuery(
                    creatorId = creatorId,
                    query = "brand voice hooks audience objections competitor gaps top content",
                    limit = 6,
                ),
            )
        }.getOrDefault(emptyList())
        val trends = runCatching {
            trendRepository.observeOpportunities(creatorId, limit = 3).first()
        }.getOrDefault(emptyList())
        val drafts = runCatching {
            contentRepository.latestDrafts(creatorId, limit = 3)
        }.getOrDefault(emptyList())
        val insights = runCatching {
            competitorRepository.latestInsights(creatorId, limit = 3)
        }.getOrDefault(emptyList())

        return """
            Brand DNA: ${brandDna?.creatorName.orEmpty()} / ${brandDna?.archetype.orEmpty()}
            Voice: ${brandDna?.voiceRulesJson.orEmpty()}
            Banned claims: ${brandDna?.bannedClaimsJson.orEmpty()}
            Goals: ${brandDna?.businessGoalsJson.orEmpty()}
            Memories: ${memories.joinToString(" | ") { "${it.title}: ${it.summary.take(180)}" }}
            Trends: ${trends.joinToString(" | ") { "${it.title} (${(it.opportunityScore * 100).toInt()}%)" }}
            Drafts: ${drafts.joinToString(" | ") { it.title }}
            Competitor gaps: ${insights.joinToString(" | ") { it.gap }}
        """.trimIndent()
    }
}

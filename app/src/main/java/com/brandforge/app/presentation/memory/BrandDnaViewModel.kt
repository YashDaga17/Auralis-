package com.brandforge.app.presentation.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.core.model.MemoryType
import com.brandforge.app.core.ai.gemini.GeminiGenerationApi
import com.brandforge.app.core.ai.gemini.GeminiGenerationConfig
import com.brandforge.app.core.ai.gemini.GeminiGenerationContent
import com.brandforge.app.core.ai.gemini.GeminiGenerationPart
import com.brandforge.app.core.ai.gemini.GeminiGenerationRequest
import com.brandforge.app.data.trend.firecrawl.FirecrawlSearchRequest
import com.brandforge.app.data.trend.firecrawl.FirecrawlScrapeRequest
import com.brandforge.app.data.trend.firecrawl.FirecrawlTrendApi
import com.brandforge.app.domain.memory.BrandDnaInput
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryShardDraft
import com.brandforge.app.domain.memory.UpsertBrandDnaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrandDnaViewModel @Inject constructor(
    private val upsertBrandDna: UpsertBrandDnaUseCase,
    private val preferencesStore: CreatorPreferencesStore,
    private val firecrawlTrendApi: FirecrawlTrendApi,
    private val geminiGenerationApi: GeminiGenerationApi,
    private val creatorMemoryRepository: CreatorMemoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BrandDnaUiState())
    val uiState: StateFlow<BrandDnaUiState> = _uiState.asStateFlow()

    fun updateCreatorId(value: String) = update { copy(creatorId = value, saveCompleted = false, errorMessage = null) }
    fun updateProfileUrl(value: String) = update { copy(profileUrl = value, profileIngestCompleted = false, errorMessage = null) }
    fun updateCreatorName(value: String) = update { copy(creatorName = value, saveCompleted = false, errorMessage = null) }
    fun updateArchetype(value: String) = update { copy(archetype = value, saveCompleted = false, errorMessage = null) }
    fun updateVoiceRules(value: String) = update { copy(voiceRulesJson = value, saveCompleted = false, errorMessage = null) }
    fun updateBannedClaims(value: String) = update { copy(bannedClaimsJson = value, saveCompleted = false, errorMessage = null) }
    fun updateBusinessGoals(value: String) = update { copy(businessGoalsJson = value, saveCompleted = false, errorMessage = null) }

    fun ingestProfile() {
        val state = _uiState.value
        val profileUrl = state.profileUrl.trim()
        if (profileUrl.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Profile URL is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    ingestingProfile = true,
                    profileIngestCompleted = false,
                    errorMessage = null,
                )
            }
            runCatching {
                val creatorId = state.creatorId.trim().ifBlank { profileUrl.toCreatorId() }
                val profileText = fetchProfileText(profileUrl)
                val extracted = extractProfile(profileUrl, profileText)
                creatorMemoryRepository.writeMemory(
                    MemoryShardDraft(
                        id = "profile-scrape-$creatorId-${profileUrl.hashCode()}",
                        creatorId = creatorId,
                        type = MemoryType.AudienceInsight,
                        title = "Profile scrape: ${profileUrl.take(80)}",
                        summary = profileText.take(1_800),
                        sourceUri = profileUrl,
                        retrievalWeight = 0.82f,
                    ),
                )
                preferencesStore.setSelectedCreatorId(creatorId)
                extracted.copy(creatorId = creatorId)
            }.onSuccess { extracted ->
                _uiState.update {
                    it.copy(
                        creatorId = extracted.creatorId,
                        creatorName = extracted.creatorName.ifBlank { it.creatorName },
                        archetype = extracted.archetype.ifBlank { it.archetype },
                        voiceRulesJson = extracted.voiceRules.ifBlank { it.voiceRulesJson },
                        bannedClaimsJson = extracted.bannedClaims.ifBlank { it.bannedClaimsJson },
                        businessGoalsJson = extracted.businessGoals.ifBlank { it.businessGoalsJson },
                        ingestingProfile = false,
                        profileIngestCompleted = true,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        ingestingProfile = false,
                        errorMessage = throwable.message ?: "Unable to scrape creator profile",
                    )
                }
            }
        }
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, errorMessage = null, saveCompleted = false) }
            runCatching {
                upsertBrandDna(
                    BrandDnaInput(
                        creatorId = state.creatorId.trim(),
                        creatorName = state.creatorName.trim(),
                        archetype = state.archetype.trim(),
                        voiceRulesJson = state.voiceRulesJson.trim(),
                        bannedClaimsJson = state.bannedClaimsJson.trim(),
                        businessGoalsJson = state.businessGoalsJson.trim(),
                    ),
                )
                preferencesStore.setSelectedCreatorId(state.creatorId.trim())
                preferencesStore.markBrandDnaOnboardingCompleted(true)
            }.onSuccess {
                _uiState.update { it.copy(saving = false, saveCompleted = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        saving = false,
                        errorMessage = throwable.message ?: "Unable to save Brand DNA",
                    )
                }
            }
        }
    }

    private fun update(reducer: BrandDnaUiState.() -> BrandDnaUiState) {
        _uiState.update { it.reducer() }
    }

    private suspend fun fetchProfileText(profileUrl: String): String {
        val normalizedUrl = profileUrl.normalizedWebUrl()
        val exactScrapeText = runCatching {
            val response = firecrawlTrendApi.scrape(
                FirecrawlScrapeRequest(
                    url = normalizedUrl,
                    formats = listOf("markdown", "summary", "links"),
                    onlyCleanContent = true,
                    mobile = true,
                ),
            )
            response.data?.let { data ->
                listOfNotNull(
                    data.metadata?.title,
                    data.metadata?.description,
                    data.summary,
                    data.markdown,
                    data.links.take(12).takeIf { it.isNotEmpty() }?.joinToString(prefix = "Links:\n", separator = "\n"),
                ).joinToString(separator = "\n")
            }.orEmpty()
        }.getOrDefault("")

        val response = firecrawlTrendApi.search(
            FirecrawlSearchRequest(
                query = "$normalizedUrl creator profile bio posts about audience niche",
                limit = 5,
            ),
        )
        val rows = response.data?.web.orEmpty().map { result ->
            listOfNotNull(
                result.title,
                result.description,
                result.markdown,
                result.url ?: result.metadata?.sourceURL ?: result.metadata?.url,
            ).joinToString(separator = "\n")
        }
        return listOf(
            "Exact URL scrape:\n$exactScrapeText",
            "Search-backed scrape:\n${rows.joinToString(separator = "\n\n")}",
        ).joinToString(separator = "\n\n")
            .trim()
            .ifBlank {
                "Profile URL: $normalizedUrl\nFirecrawl returned no profile text. Use the URL and creator-supplied fields as source."
            }
    }

    private suspend fun extractProfile(profileUrl: String, profileText: String): ExtractedProfile {
        val prompt = """
            You are BrandForge Brand DNA Agent.
            Extract a creator brand profile from the scraped profile text.
            Return exactly these labelled lines, no markdown:
            CREATOR_NAME:
            ARCHETYPE:
            VOICE_RULES_JSON:
            BANNED_CLAIMS_JSON:
            BUSINESS_GOALS_JSON:

            Profile URL:
            $profileUrl

            Scraped profile text:
            ${profileText.take(6_000)}
        """.trimIndent()
        val response = geminiGenerationApi.generateContent(
            model = "models/gemini-2.5-flash",
            request = GeminiGenerationRequest(
                contents = listOf(
                    GeminiGenerationContent(
                        parts = listOf(GeminiGenerationPart(prompt)),
                    ),
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.25,
                    maxOutputTokens = 900,
                ),
            ),
        )
        val text = response.candidates.firstOrNull()?.content?.parts?.joinToString("\n") { it.text.orEmpty() }.orEmpty()
        return ExtractedProfile(
            creatorName = text.lineValue("CREATOR_NAME"),
            archetype = text.lineValue("ARCHETYPE"),
            voiceRules = text.lineValue("VOICE_RULES_JSON").ifBlank {
                """["Use profile-specific vocabulary","Stay consistent with creator niche","Avoid generic AI phrasing"]"""
            },
            bannedClaims = text.lineValue("BANNED_CLAIMS_JSON").ifBlank {
                """["Avoid unsupported claims","Avoid offensive or unsafe content","Do not promise guaranteed virality"]"""
            },
            businessGoals = text.lineValue("BUSINESS_GOALS_JSON").ifBlank {
                """["Grow audience trust","Create profile-aligned content","Turn attention into qualified opportunities"]"""
            },
        )
    }

    private fun String.lineValue(label: String): String =
        lineSequence()
            .firstOrNull { it.trim().startsWith("$label:", ignoreCase = true) }
            ?.substringAfter(":")
            ?.trim()
            .orEmpty()

    private fun String.toCreatorId(): String =
        lowercase(Locale.US)
            .removePrefix("https://")
            .removePrefix("http://")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "creator" }
            .take(48)

    private fun String.normalizedWebUrl(): String {
        val trimmed = trim()
        return when {
            trimmed.startsWith("http://", ignoreCase = true) -> trimmed
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> "https://$trimmed"
        }
    }

    private data class ExtractedProfile(
        val creatorId: String = "",
        val creatorName: String = "",
        val archetype: String = "",
        val voiceRules: String = "",
        val bannedClaims: String = "",
        val businessGoals: String = "",
    )
}

package com.brandforge.app.core.debug

import com.brandforge.app.core.ai.EmbeddingClient
import com.brandforge.app.core.ai.gemini.GeminiGenerationApi
import com.brandforge.app.core.ai.gemini.GeminiGenerationConfig
import com.brandforge.app.core.ai.gemini.GeminiGenerationContent
import com.brandforge.app.core.ai.gemini.GeminiGenerationPart
import com.brandforge.app.core.ai.gemini.GeminiGenerationRequest
import com.brandforge.app.core.ai.openrouter.OpenRouterChatRequest
import com.brandforge.app.core.ai.openrouter.OpenRouterClient
import com.brandforge.app.core.ai.openrouter.OpenRouterMessage
import com.brandforge.app.core.config.EnvironmentKey
import com.brandforge.app.core.config.EnvironmentManager
import com.brandforge.app.core.database.BrandForgeDatabase
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.core.model.MemoryType
import com.brandforge.app.core.network.IoDispatcher
import com.brandforge.app.data.memory.QdrantMemoryRemoteDataSource
import com.brandforge.app.data.trend.firecrawl.FirecrawlSearchRequest
import com.brandforge.app.data.trend.firecrawl.FirecrawlTrendApi
import com.brandforge.app.data.trend.youtube.YouTubeTrendApi
import com.brandforge.app.domain.competitor.CompetitorRepository
import com.brandforge.app.domain.content.ContentRepository
import com.brandforge.app.domain.lead.LeadRepository
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryQuery
import com.brandforge.app.domain.trend.TrendRepository
import com.brandforge.app.domain.twin.TwinChatRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class AppHealthCheckManager @Inject constructor(
    private val database: BrandForgeDatabase,
    private val preferencesStore: CreatorPreferencesStore,
    private val environmentManager: EnvironmentManager,
    private val openRouterClient: OpenRouterClient,
    private val geminiGenerationApi: GeminiGenerationApi,
    private val firecrawlTrendApi: FirecrawlTrendApi,
    private val youTubeTrendApi: YouTubeTrendApi,
    private val embeddingClient: EmbeddingClient,
    private val qdrantMemoryRemoteDataSource: QdrantMemoryRemoteDataSource,
    private val creatorMemoryRepository: CreatorMemoryRepository,
    private val trendRepository: TrendRepository,
    private val contentRepository: ContentRepository,
    private val twinChatRepository: TwinChatRepository,
    private val leadRepository: LeadRepository,
    private val competitorRepository: CompetitorRepository,
    private val errorLogger: GlobalErrorLogger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun runHealthChecks(): AppHealthSnapshot =
        withContext(ioDispatcher) {
            val creatorId = activeCreatorId()
            val results = listOf(
                checkRoom(),
                checkDataStore(),
                checkOpenRouter(),
                checkGemini(),
                checkFirecrawl(),
                checkYouTube(),
                checkQdrant(creatorId),
                checkMemoryRetrieval(creatorId),
                checkTrendRetrieval(creatorId),
                checkContentGenerationReadiness(creatorId),
                checkTwinChatReadiness(creatorId),
                checkLeadDetectionReadiness(creatorId),
                checkCompetitorIntelligenceReadiness(creatorId),
            )
            AppHealthSnapshot(
                checkedAt = System.currentTimeMillis(),
                results = results,
            )
        }

    suspend fun activeCreatorId(): String =
        preferencesStore.selectedCreatorId.first().orEmpty().ifBlank { DefaultCreatorId }

    private suspend fun checkRoom(): HealthCheckResult =
        safeCheck("Room Database") {
            val open = database.openHelper.writableDatabase.isOpen
            if (open) pass("brandforge.db is open at schema version ${database.openHelper.writableDatabase.version}") else fail("Room returned a closed database handle.")
        }

    private suspend fun checkDataStore(): HealthCheckResult =
        safeCheck("DataStore") {
            val lastValidation = preferencesStore.lastStartupValidationEpochMillis.first()
            pass("Preferences readable. Last startup validation epoch: $lastValidation")
        }

    private suspend fun checkOpenRouter(): HealthCheckResult =
        remoteCheck(
            name = "OpenRouter",
            key = EnvironmentKey.OpenRouterApiKey,
        ) {
            val response = withTimeout(RemoteTimeoutMillis) {
                openRouterClient.createChatCompletion(
                    OpenRouterChatRequest(
                        model = "minimax/minimax-01",
                        messages = listOf(
                            OpenRouterMessage(
                                role = "user",
                                content = "Reply with OK only for BrandForge health check.",
                            ),
                        ),
                        temperature = 0.0,
                        maxTokens = 8,
                    ),
                )
            }
            val content = response.choices.firstOrNull()?.message?.content.orEmpty()
            if (content.isNotBlank()) pass("Chat completion returned a response.") else warning("Response was empty.")
        }

    private suspend fun checkGemini(): HealthCheckResult =
        remoteCheck(
            name = "Gemini",
            key = EnvironmentKey.GeminiApiKey,
        ) {
            val response = withTimeout(RemoteTimeoutMillis) {
                geminiGenerationApi.generateContent(
                    model = "models/gemini-2.5-flash",
                    request = GeminiGenerationRequest(
                        contents = listOf(
                            GeminiGenerationContent(
                                parts = listOf(
                                    GeminiGenerationPart("Reply with OK only for BrandForge health check."),
                                ),
                            ),
                        ),
                        generationConfig = GeminiGenerationConfig(
                            temperature = 0.0,
                            maxOutputTokens = 8,
                        ),
                    ),
                )
            }
            val content = response.candidates.firstOrNull()?.content?.parts?.joinToString("\n") { it.text.orEmpty() }.orEmpty()
            if (content.isNotBlank()) pass("Generation API returned a response.") else warning("Response was empty.")
        }

    private suspend fun checkFirecrawl(): HealthCheckResult =
        remoteCheck(
            name = "Firecrawl",
            key = EnvironmentKey.FirecrawlApiKey,
        ) {
            val response = withTimeout(RemoteTimeoutMillis) {
                firecrawlTrendApi.search(
                    FirecrawlSearchRequest(
                        query = "BrandForge creator economy India",
                        limit = 1,
                    ),
                )
            }
            when {
                response.success == false -> fail(response.warning ?: "Firecrawl returned success=false.")
                response.data?.web.orEmpty().isNotEmpty() -> pass("Search returned ${response.data?.web.orEmpty().size} web result(s).")
                else -> warning(response.warning ?: "Search succeeded but returned no web results.")
            }
        }

    private suspend fun checkYouTube(): HealthCheckResult =
        remoteCheck(
            name = "YouTube API",
            key = EnvironmentKey.YouTubeApiKey,
        ) {
            val response = withTimeout(RemoteTimeoutMillis) {
                youTubeTrendApi.search(
                    query = "creator economy India",
                    maxResults = 1,
                )
            }
            if (response.items.isNotEmpty()) pass("Search returned ${response.items.size} video result(s).") else warning("Search succeeded but returned no videos.")
        }

    private suspend fun checkQdrant(creatorId: String): HealthCheckResult =
        remoteCheck(
            name = "Qdrant",
            key = EnvironmentKey.QdrantApiKey,
        ) {
            if (!isConfigured(EnvironmentKey.QdrantUrl)) {
                return@remoteCheck warning("QDRANT_URL is missing.")
            }
            val embedding = withTimeout(RemoteTimeoutMillis) {
                embeddingClient.embed("BrandForge Qdrant health check")
            }
            val scores = withTimeout(RemoteTimeoutMillis) {
                qdrantMemoryRemoteDataSource.search(
                    creatorId = creatorId,
                    embedding = embedding,
                    limit = 1,
                )
            }
            pass("Vector search reachable. Matches returned: ${scores.size}")
        }

    private suspend fun checkMemoryRetrieval(creatorId: String): HealthCheckResult =
        safeCheck("Memory Retrieval") {
            val brandDnaReady = withTimeoutOrNull(LocalTimeoutMillis) {
                creatorMemoryRepository.observeBrandDna(creatorId).first()
            } != null
            val memories = withTimeout(RemoteTimeoutMillis) {
                creatorMemoryRepository.retrieve(
                    MemoryQuery(
                        creatorId = creatorId,
                        query = "What should this creator post next?",
                        limit = 5,
                        types = MemoryType.entries,
                    ),
                )
            }
            when {
                memories.isNotEmpty() -> pass("Retrieved ${memories.size} relevant memory shard(s). Brand DNA ready=$brandDnaReady")
                brandDnaReady -> warning("Brand DNA exists, but no memory shards matched the debug query.")
                else -> warning("No Brand DNA or memory found for active creator '$creatorId'. Use Debug Seed or complete onboarding.")
            }
        }

    private suspend fun checkTrendRetrieval(creatorId: String): HealthCheckResult =
        safeCheck("Trend Retrieval") {
            val opportunities = trendRepository.observeOpportunities(creatorId, limit = 5).first()
            if (opportunities.isNotEmpty()) {
                pass("Loaded ${opportunities.size} persisted opportunity/opportunities.")
            } else {
                warning("No persisted opportunities for '$creatorId'. Run Trend Radar or Debug Seed.")
            }
        }

    private suspend fun checkContentGenerationReadiness(creatorId: String): HealthCheckResult =
        safeCheck("Content Generation") {
            val drafts = contentRepository.latestDrafts(creatorId, limit = 5)
            if (drafts.isNotEmpty()) pass("Loaded ${drafts.size} generated draft(s).") else warning("No generated drafts yet. Generate from a trend opportunity.")
        }

    private suspend fun checkTwinChatReadiness(creatorId: String): HealthCheckResult =
        safeCheck("Twin Chat") {
            val messages = twinChatRepository.latestMessages(creatorId, limit = 6)
            if (messages.isNotEmpty()) pass("Loaded ${messages.size} persisted chat message(s).") else warning("No Twin Chat history yet.")
        }

    private suspend fun checkLeadDetectionReadiness(creatorId: String): HealthCheckResult =
        safeCheck("Lead Detection") {
            val leads = leadRepository.latestByCreator(creatorId, limit = 5)
            if (leads.isNotEmpty()) pass("Loaded ${leads.size} classified lead/audience item(s).") else warning("No classified leads yet.")
        }

    private suspend fun checkCompetitorIntelligenceReadiness(creatorId: String): HealthCheckResult =
        safeCheck("Competitor Intelligence") {
            val insights = competitorRepository.latestInsights(creatorId, limit = 5)
            if (insights.isNotEmpty()) pass("Loaded ${insights.size} competitor insight(s).") else warning("No competitor insights yet.")
        }

    private suspend fun remoteCheck(
        name: String,
        key: EnvironmentKey,
        block: suspend () -> HealthCheckResult,
    ): HealthCheckResult {
        if (!isConfigured(key)) {
            return HealthCheckResult(
                name = name,
                status = HealthStatus.Fail,
                reason = "${key.buildConfigName} is missing from BuildConfig/environment.",
            )
        }
        return safeCheck(name, block)
    }

    private fun isConfigured(key: EnvironmentKey): Boolean =
        environmentManager.validate().secrets.firstOrNull { it.key == key }?.configured == true

    private suspend fun safeCheck(
        name: String,
        block: suspend () -> HealthCheckResult,
    ): HealthCheckResult =
        runCatching {
            block().let { result ->
                if (result.name.isBlank()) result.copy(name = name) else result
            }
        }.getOrElse { throwable ->
            errorLogger.logBlocking(
                feature = name,
                screen = "Debug Panel",
                throwable = throwable,
                severity = DebugErrorSeverity.Error,
            )
            fail(throwable.message ?: throwable::class.java.simpleName, name)
        }

    private fun pass(reason: String, name: String = ""): HealthCheckResult =
        HealthCheckResult(name = name, status = HealthStatus.Pass, reason = reason)

    private fun warning(reason: String, name: String = ""): HealthCheckResult =
        HealthCheckResult(name = name, status = HealthStatus.Warning, reason = reason)

    private fun fail(reason: String, name: String = ""): HealthCheckResult =
        HealthCheckResult(name = name, status = HealthStatus.Fail, reason = reason)

    private companion object {
        const val DefaultCreatorId = "debug-demo-creator"
        const val LocalTimeoutMillis = 1_500L
        const val RemoteTimeoutMillis = 8_000L
    }
}

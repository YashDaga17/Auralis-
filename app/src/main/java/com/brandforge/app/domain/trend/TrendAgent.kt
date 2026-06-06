package com.brandforge.app.domain.trend

import com.brandforge.app.domain.memory.BrandDna
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import com.brandforge.app.domain.memory.MemoryQuery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class TrendAgent @Inject constructor(
    private val trendRepository: TrendRepository,
    private val creatorMemoryRepository: CreatorMemoryRepository,
    private val scoreTrendOpportunity: ScoreTrendOpportunityUseCase,
) {
    suspend fun scan(creatorId: String): TrendScanResult {
        require(creatorId.isNotBlank()) { "creatorId is required" }
        val brandDna = loadBrandDna(creatorId)
        val signals = trendRepository.fetchSignals(
            TrendSignalQuery(
                creatorId = creatorId,
                query = brandDna.toTrendQuery(),
                limit = SourceLimit,
            ),
        )
        val persistedSignals = trendRepository.persistSignals(signals)
        val opportunities = persistedSignals.map { signal ->
            val relevantMemories = creatorMemoryRepository.retrieve(
                MemoryQuery(
                    creatorId = creatorId,
                    query = signal.memoryRetrievalQuery(brandDna),
                    limit = MemoryLimit,
                ),
            )
            scoreTrendOpportunity.score(
                signal = signal,
                brandDna = brandDna,
                retrievedMemories = relevantMemories,
            )
        }
        val persistedOpportunities = trendRepository.persistOpportunities(opportunities)
        return TrendScanResult(
            signals = persistedSignals,
            opportunities = persistedOpportunities,
        )
    }

    private suspend fun loadBrandDna(creatorId: String): BrandDna =
        withTimeoutOrNull(BrandDnaLoadTimeoutMillis) {
            creatorMemoryRepository.observeBrandDna(creatorId).first()
        } ?: error("Brand DNA is required before trend intelligence can run")

    private fun BrandDna.toTrendQuery(): String =
        listOf(
            archetype,
            businessGoalsJson.toSearchTerms(),
            voiceRulesJson.toSearchTerms(),
        )
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
            .ifBlank { creatorName }
            .take(MaxQueryLength)

    private fun TrendSignal.memoryRetrievalQuery(brandDna: BrandDna): String =
        listOf(
            title,
            summary,
            sourcePlatform,
            brandDna.businessGoalsJson,
            brandDna.archetype,
        ).joinToString(separator = "\n")

    private fun String.toSearchTerms(): String =
        lowercase()
            .split(Regex("[^a-z0-9]+"))
            .asSequence()
            .filter { it.length > 3 }
            .distinct()
            .take(8)
            .joinToString(separator = " ")

    private companion object {
        const val SourceLimit = 8
        const val MemoryLimit = 8
        const val MaxQueryLength = 500
        const val BrandDnaLoadTimeoutMillis = 2_000L
    }
}

package com.brandforge.app.domain.memory

import javax.inject.Inject

class MemoryRetrievalEngine @Inject constructor(
    private val scoreMemoryRelevance: ScoreMemoryRelevanceUseCase,
) {
    fun rank(
        query: MemoryQuery,
        localMemories: List<MemoryShard>,
        remoteScoresById: Map<String, Float>,
    ): List<MemoryShard> =
        localMemories
            .distinctBy { it.id }
            .sortedByDescending { memory ->
                scoreMemoryRelevance.score(
                    query = query.query,
                    memory = memory,
                    remoteScore = remoteScoresById[memory.id],
                )
            }
            .take(query.limit.coerceAtLeast(1))
}

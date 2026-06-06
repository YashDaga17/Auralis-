package com.brandforge.app.domain.memory

import javax.inject.Inject
import kotlin.math.min

class ScoreMemoryRelevanceUseCase @Inject constructor() {
    fun score(query: String, memory: MemoryShard, remoteScore: Float? = null): Float {
        val normalizedQuery = query.lowercase()
            .split(Regex("\\W+"))
            .filter { it.length > 2 }
            .toSet()

        val searchable = (memory.title + " " + memory.summary).lowercase()
        val lexicalScore = if (normalizedQuery.isEmpty()) {
            0f
        } else {
            normalizedQuery.count { searchable.contains(it) }.toFloat() / normalizedQuery.size
        }

        val remoteBoost = remoteScore ?: 0f
        return min(
            1f,
            (memory.retrievalWeight * 0.45f) +
                (lexicalScore * 0.35f) +
                (remoteBoost * 0.20f),
        )
    }
}

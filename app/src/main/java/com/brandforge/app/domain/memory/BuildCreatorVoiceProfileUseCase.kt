package com.brandforge.app.domain.memory

import com.brandforge.app.core.model.MemoryType
import javax.inject.Inject

class BuildCreatorVoiceProfileUseCase @Inject constructor(
    private val repository: CreatorMemoryRepository,
) {
    suspend operator fun invoke(creatorId: String): List<MemoryShard> =
        repository.retrieve(
            MemoryQuery(
                creatorId = creatorId,
                query = "brand voice vocabulary claims goals audience preferences",
                limit = 12,
                types = listOf(
                    MemoryType.BrandDna,
                    MemoryType.PastContent,
                    MemoryType.AudienceInsight,
                    MemoryType.PerformanceHistory,
                ),
            ),
        )
}

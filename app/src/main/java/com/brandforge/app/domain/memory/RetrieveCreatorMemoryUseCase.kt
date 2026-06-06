package com.brandforge.app.domain.memory

import javax.inject.Inject

class RetrieveCreatorMemoryUseCase @Inject constructor(
    private val repository: CreatorMemoryRepository,
) {
    suspend operator fun invoke(query: MemoryQuery): List<MemoryShard> {
        require(query.creatorId.isNotBlank()) { "creatorId is required" }
        return repository.retrieve(query)
    }
}

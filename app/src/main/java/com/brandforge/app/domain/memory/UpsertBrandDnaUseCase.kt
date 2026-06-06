package com.brandforge.app.domain.memory

import javax.inject.Inject

class UpsertBrandDnaUseCase @Inject constructor(
    private val repository: CreatorMemoryRepository,
) {
    suspend operator fun invoke(input: BrandDnaInput) {
        require(input.creatorId.isNotBlank()) { "creatorId is required" }
        require(input.creatorName.isNotBlank()) { "creatorName is required" }
        repository.upsertBrandDna(input)
    }
}

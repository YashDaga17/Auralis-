package com.brandforge.app.domain.memory

import javax.inject.Inject

class MemoryAgent @Inject constructor(
    private val retrieveCreatorMemory: RetrieveCreatorMemoryUseCase,
    private val upsertBrandDna: UpsertBrandDnaUseCase,
) {
    suspend fun retrieveContext(query: MemoryQuery): List<MemoryShard> =
        retrieveCreatorMemory(query)

    suspend fun learnBrandDna(input: BrandDnaInput) {
        upsertBrandDna(input)
    }
}

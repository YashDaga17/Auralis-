package com.brandforge.app.presentation.memory

import com.brandforge.app.domain.memory.MemoryShard

data class MemoryUiState(
    val creatorId: String = "",
    val query: String = "",
    val results: List<MemoryShard> = emptyList(),
    val loading: Boolean = false,
    val errorMessage: String? = null,
)

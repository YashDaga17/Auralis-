package com.brandforge.app.presentation.twin

import com.brandforge.app.domain.twin.TwinChatMessage

data class TwinChatUiState(
    val creatorId: String = "",
    val draftMessage: String = "",
    val messages: List<TwinChatMessage> = emptyList(),
    val sending: Boolean = false,
    val errorMessage: String? = null,
)

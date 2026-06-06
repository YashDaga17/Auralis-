package com.brandforge.app.presentation.warroom

data class WarRoomUiState(
    val creatorId: String = "",
    val brief: String = "",
    val runningBattle: Boolean = false,
    val battleResult: String = "",
    val errorMessage: String? = null,
)

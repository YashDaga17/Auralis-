package com.brandforge.app.presentation.memory

data class BrandDnaUiState(
    val creatorId: String = "",
    val profileUrl: String = "",
    val creatorName: String = "",
    val archetype: String = "",
    val voiceRulesJson: String = "",
    val bannedClaimsJson: String = "",
    val businessGoalsJson: String = "",
    val ingestingProfile: Boolean = false,
    val profileIngestCompleted: Boolean = false,
    val saving: Boolean = false,
    val saveCompleted: Boolean = false,
    val errorMessage: String? = null,
)

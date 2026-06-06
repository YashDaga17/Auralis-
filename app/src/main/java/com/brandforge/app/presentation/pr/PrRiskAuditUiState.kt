package com.brandforge.app.presentation.pr

data class PrRiskAuditUiState(
    val creatorId: String = "",
    val mediaUri: String = "",
    val mediaContext: String = "",
    val caption: String = "",
    val auditing: Boolean = false,
    val report: String = "",
    val errorMessage: String? = null,
)

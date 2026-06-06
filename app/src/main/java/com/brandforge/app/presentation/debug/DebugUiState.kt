package com.brandforge.app.presentation.debug

import com.brandforge.app.core.config.EnvironmentValidation
import com.brandforge.app.core.debug.AppHealthSnapshot
import com.brandforge.app.core.debug.DebugChecklistItem
import com.brandforge.app.core.debug.DebugErrorLog

data class DebugUiState(
    val appVersion: String = "",
    val buildType: String = "",
    val databaseVersion: Int = 0,
    val activeCreatorId: String = "",
    val lastStartupValidationEpochMillis: Long = 0L,
    val environmentValidation: EnvironmentValidation? = null,
    val healthSnapshot: AppHealthSnapshot? = null,
    val errors: List<DebugErrorLog> = emptyList(),
    val checklist: List<DebugChecklistItem> = emptyList(),
    val runningHealthCheck: Boolean = false,
    val seeding: Boolean = false,
    val lastSeedSummary: String? = null,
    val message: String? = null,
)

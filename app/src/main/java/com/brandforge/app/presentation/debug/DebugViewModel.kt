package com.brandforge.app.presentation.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandforge.app.BuildConfig
import com.brandforge.app.core.config.EnvironmentManager
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import com.brandforge.app.core.debug.AppHealthCheckManager
import com.brandforge.app.core.debug.DebugChecklistManager
import com.brandforge.app.core.debug.DebugChecklistStatus
import com.brandforge.app.core.debug.DebugErrorSeverity
import com.brandforge.app.core.debug.DebugSeedDataGenerator
import com.brandforge.app.core.debug.GlobalErrorLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val healthCheckManager: AppHealthCheckManager,
    private val seedDataGenerator: DebugSeedDataGenerator,
    private val checklistManager: DebugChecklistManager,
    private val errorLogger: GlobalErrorLogger,
    private val environmentManager: EnvironmentManager,
    private val preferencesStore: CreatorPreferencesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        DebugUiState(
            appVersion = BuildConfig.VERSION_NAME,
            buildType = BuildConfig.BUILD_TYPE,
            databaseVersion = DatabaseVersion,
            environmentValidation = environmentManager.validate(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            checklistManager.ensureDefaults()
        }
        viewModelScope.launch {
            combine(
                preferencesStore.selectedCreatorId,
                preferencesStore.lastStartupValidationEpochMillis,
                errorLogger.observeLatest(),
                checklistManager.observeChecklist(),
            ) { creatorId, lastStartupValidation, errors, checklist ->
                _uiState.update { state ->
                    state.copy(
                        activeCreatorId = creatorId.orEmpty().ifBlank { DefaultCreatorId },
                        lastStartupValidationEpochMillis = lastStartupValidation,
                        environmentValidation = environmentManager.validate(),
                        errors = errors,
                        checklist = checklist,
                    )
                }
            }.collect {}
        }
    }

    fun runHealthCheck() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    runningHealthCheck = true,
                    message = null,
                    environmentValidation = environmentManager.validate(),
                )
            }
            runCatching {
                healthCheckManager.runHealthChecks()
            }.onSuccess { snapshot ->
                _uiState.update {
                    it.copy(
                        runningHealthCheck = false,
                        healthSnapshot = snapshot,
                        message = "Health check complete: ${snapshot.overallStatus.name.uppercase()}",
                    )
                }
            }.onFailure { throwable ->
                errorLogger.logBlocking(
                    feature = "Health Check",
                    screen = "Debug Panel",
                    throwable = throwable,
                    severity = DebugErrorSeverity.Error,
                )
                _uiState.update {
                    it.copy(
                        runningHealthCheck = false,
                        message = throwable.message ?: "Health check failed.",
                    )
                }
            }
        }
    }

    fun seedDebugData() {
        viewModelScope.launch {
            val creatorId = uiState.value.activeCreatorId.ifBlank { DefaultCreatorId }
            _uiState.update {
                it.copy(
                    seeding = true,
                    message = null,
                )
            }
            runCatching {
                seedDataGenerator.seedAll(creatorId)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        seeding = false,
                        lastSeedSummary = result.summary,
                        message = result.summary,
                    )
                }
            }.onFailure { throwable ->
                errorLogger.logBlocking(
                    feature = "Debug Seed",
                    screen = "Debug Panel",
                    throwable = throwable,
                    severity = DebugErrorSeverity.Error,
                )
                _uiState.update {
                    it.copy(
                        seeding = false,
                        message = throwable.message ?: "Debug seed failed.",
                    )
                }
            }
        }
    }

    fun updateChecklistStatus(itemId: String, status: DebugChecklistStatus) {
        viewModelScope.launch {
            checklistManager.updateStatus(itemId, status)
        }
    }

    fun clearErrors() {
        viewModelScope.launch {
            errorLogger.clear()
        }
    }

    private companion object {
        const val DatabaseVersion = 8
        const val DefaultCreatorId = "debug-demo-creator"
    }
}

package com.brandforge.app.presentation.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brandforge.app.core.config.EnvironmentKey
import com.brandforge.app.core.debug.DebugChecklistItem
import com.brandforge.app.core.debug.DebugChecklistStatus
import com.brandforge.app.core.debug.DebugErrorSeverity
import com.brandforge.app.core.debug.DebugErrorLog
import com.brandforge.app.core.debug.HealthCheckResult
import com.brandforge.app.core.debug.HealthStatus
import com.brandforge.app.core.model.RiskLevel
import com.brandforge.app.ui.components.ForgePanel
import com.brandforge.app.ui.components.KeyValueLine
import com.brandforge.app.ui.components.PixelButton
import com.brandforge.app.ui.components.SectionHeader
import com.brandforge.app.ui.components.StatusPill
import com.brandforge.app.ui.components.TerminalDivider
import com.brandforge.app.ui.theme.ForgeColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebugPanelScreen(
    state: DebugUiState,
    onRunHealthCheck: () -> Unit,
    onSeedDebugData: () -> Unit,
    onChecklistStatusChange: (String, DebugChecklistStatus) -> Unit,
    onClearErrors: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            DebugHeaderPanel(
                state = state,
                onRunHealthCheck = onRunHealthCheck,
                onSeedDebugData = onSeedDebugData,
                onClose = onClose,
            )
        }
        item {
            EnvironmentPanel(state)
        }
        item {
            HealthPanel(state)
        }
        item {
            ChecklistHeader()
        }
        items(
            items = state.checklist,
            key = { it.id },
        ) { item ->
            ChecklistItemPanel(
                item = item,
                onChecklistStatusChange = onChecklistStatusChange,
            )
        }
        item {
            ErrorLogPanel(
                errors = state.errors,
                onClearErrors = onClearErrors,
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DebugHeaderPanel(
    state: DebugUiState,
    onRunHealthCheck: () -> Unit,
    onSeedDebugData: () -> Unit,
    onClose: () -> Unit,
) {
    ForgePanel {
        SectionHeader(
            title = "DEBUG PANEL",
            trailing = state.healthSnapshot?.overallStatus?.name?.uppercase() ?: "HIDDEN",
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeyValueLine(label = "App Version", value = state.appVersion.ifBlank { "unknown" })
        KeyValueLine(label = "Build Type", value = state.buildType.ifBlank { "unknown" })
        KeyValueLine(label = "Database Version", value = state.databaseVersion.toString())
        KeyValueLine(label = "Active Creator ID", value = state.activeCreatorId.ifBlank { "none" })
        KeyValueLine(label = "Last Sync Time", value = formatEpoch(state.lastStartupValidationEpochMillis))
        state.message?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ForgeColor.Yellow,
            )
        }
        state.lastSeedSummary?.let { summary ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = ForgeColor.Green,
            )
        }
        TerminalDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PixelButton(
                label = if (state.runningHealthCheck) "RUNNING" else "HEALTH",
                onClick = onRunHealthCheck,
                modifier = Modifier.weight(1f),
                active = state.runningHealthCheck,
            )
            PixelButton(
                label = if (state.seeding) "SEEDING" else "SEED",
                onClick = onSeedDebugData,
                modifier = Modifier.weight(1f),
                active = state.seeding,
            )
            PixelButton(
                label = "CLOSE",
                onClick = onClose,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun EnvironmentPanel(state: DebugUiState) {
    val validation = state.environmentValidation
    ForgePanel {
        SectionHeader(
            title = "ENVIRONMENT STATUS",
            trailing = if (validation?.isProductionReady == true) "READY" else "CHECK",
        )
        Spacer(modifier = Modifier.height(8.dp))
        validation?.secrets.orEmpty().forEach { secret ->
            val risk = when {
                secret.configured -> RiskLevel.Low
                secret.key.requiredForProduction -> RiskLevel.High
                else -> RiskLevel.Medium
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = secret.key.buildConfigName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                StatusPill(
                    label = if (secret.configured) "PRESENT" else "MISSING",
                    riskLevel = risk,
                )
            }
            Text(
                text = redactedStatus(secret.key, secret.redactedValue),
                style = MaterialTheme.typography.bodySmall,
                color = ForgeColor.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        validation?.endpointBaseUrls.orEmpty().forEach { (endpoint, url) ->
            KeyValueLine(label = endpoint.name, value = url)
        }
    }
}

@Composable
private fun HealthPanel(state: DebugUiState) {
    ForgePanel {
        SectionHeader(
            title = "HEALTH CHECKS",
            trailing = state.healthSnapshot?.let { formatEpoch(it.checkedAt) } ?: "NOT RUN",
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (state.healthSnapshot == null) {
            Text(
                text = "Tap HEALTH to verify Room, DataStore, APIs, memory, trends, content, Twin Chat, leads, and competitor intelligence.",
                style = MaterialTheme.typography.bodyMedium,
                color = ForgeColor.Muted,
            )
        } else {
            state.healthSnapshot.results.forEach { result ->
                HealthResultRow(result)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HealthResultRow(result: HealthCheckResult) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = result.name,
                style = MaterialTheme.typography.bodyMedium,
                color = ForgeColor.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            StatusPill(
                label = result.status.name,
                riskLevel = result.status.toRiskLevel(),
            )
        }
        Text(
            text = result.reason,
            style = MaterialTheme.typography.bodySmall,
            color = ForgeColor.Muted,
        )
    }
}

@Composable
private fun ChecklistHeader() {
    ForgePanel {
        SectionHeader(title = "REAL DEVICE CHECKLIST", trailing = "PHONE QA")
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Mark each item during physical Android testing. Status persists locally for restart validation.",
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
        )
    }
}

@Composable
private fun ChecklistItemPanel(
    item: DebugChecklistItem,
    onChecklistStatusChange: (String, DebugChecklistStatus) -> Unit,
) {
    ForgePanel(
        borderColor = item.status.toBorderColor(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = ForgeColor.Yellow,
                )
                Text(
                    text = item.criticality,
                    style = MaterialTheme.typography.labelMedium,
                    color = ForgeColor.Muted,
                )
            }
            StatusPill(
                label = item.status.label,
                riskLevel = item.status.toRiskLevel(),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Expected: ${item.expectedResult}",
            style = MaterialTheme.typography.bodySmall,
            color = ForgeColor.White,
        )
        Text(
            text = "Fail if: ${item.failureConditions}",
            style = MaterialTheme.typography.bodySmall,
            color = ForgeColor.Muted,
        )
        if (item.updatedAt > 0L) {
            Text(
                text = "Updated: ${formatEpoch(item.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = ForgeColor.Muted,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PixelButton(
                label = "NT",
                onClick = { onChecklistStatusChange(item.id, DebugChecklistStatus.NotTested) },
                modifier = Modifier.weight(1f),
                active = item.status == DebugChecklistStatus.NotTested,
            )
            PixelButton(
                label = "PASS",
                onClick = { onChecklistStatusChange(item.id, DebugChecklistStatus.Pass) },
                modifier = Modifier.weight(1f),
                active = item.status == DebugChecklistStatus.Pass,
            )
            PixelButton(
                label = "FAIL",
                onClick = { onChecklistStatusChange(item.id, DebugChecklistStatus.Fail) },
                modifier = Modifier.weight(1f),
                active = item.status == DebugChecklistStatus.Fail,
            )
        }
    }
}

@Composable
private fun ErrorLogPanel(
    errors: List<DebugErrorLog>,
    onClearErrors: () -> Unit,
) {
    ForgePanel {
        SectionHeader(title = "ERROR CAPTURE", trailing = "${errors.size} LOGS")
        Spacer(modifier = Modifier.height(8.dp))
        if (errors.isEmpty()) {
            Text(
                text = "No captured failures yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = ForgeColor.Green,
            )
        } else {
            errors.take(12).forEach { error ->
                Text(
                    text = "${formatEpoch(error.timestamp)} / ${error.severity.name.uppercase()} / ${error.feature}",
                    style = MaterialTheme.typography.labelMedium,
                    color = error.severity.toRiskColor(),
                )
                Text(
                    text = error.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = ForgeColor.White,
                )
                Text(
                    text = "Screen: ${error.screen}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ForgeColor.Muted,
                )
                TerminalDivider()
            }
        }
        PixelButton(
            label = "CLEAR ERROR LOG",
            onClick = onClearErrors,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun redactedStatus(key: EnvironmentKey, redactedValue: String): String =
    if (redactedValue.isBlank()) {
        if (key.requiredForProduction) "required" else "optional"
    } else {
        redactedValue
    }

private fun HealthStatus.toRiskLevel(): RiskLevel =
    when (this) {
        HealthStatus.Pass -> RiskLevel.Low
        HealthStatus.Warning -> RiskLevel.Medium
        HealthStatus.Fail -> RiskLevel.High
    }

private fun DebugChecklistStatus.toRiskLevel(): RiskLevel =
    when (this) {
        DebugChecklistStatus.Pass -> RiskLevel.Low
        DebugChecklistStatus.NotTested -> RiskLevel.Medium
        DebugChecklistStatus.Fail -> RiskLevel.High
    }

private fun DebugChecklistStatus.toBorderColor() =
    when (this) {
        DebugChecklistStatus.Pass -> ForgeColor.Green.copy(alpha = 0.72f)
        DebugChecklistStatus.NotTested -> ForgeColor.White.copy(alpha = 0.5f)
        DebugChecklistStatus.Fail -> ForgeColor.Red.copy(alpha = 0.82f)
    }

private fun DebugErrorSeverity.toRiskColor() =
    when (this) {
        DebugErrorSeverity.Warning -> ForgeColor.Yellow
        DebugErrorSeverity.Error -> ForgeColor.Red
        DebugErrorSeverity.Fatal -> ForgeColor.Red
    }

private fun formatEpoch(epochMillis: Long): String {
    if (epochMillis <= 0L) return "never"
    return SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(epochMillis))
}

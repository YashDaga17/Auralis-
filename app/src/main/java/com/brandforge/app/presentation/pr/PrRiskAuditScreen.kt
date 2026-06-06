package com.brandforge.app.presentation.pr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brandforge.app.core.model.RiskLevel
import com.brandforge.app.ui.components.ForgePanel
import com.brandforge.app.ui.components.KeyValueLine
import com.brandforge.app.ui.components.PixelButton
import com.brandforge.app.ui.components.SectionHeader
import com.brandforge.app.ui.components.StatusPill
import com.brandforge.app.ui.theme.ForgeColor

@Composable
fun PrRiskAuditScreen(
    state: PrRiskAuditUiState,
    onCreatorIdChange: (String) -> Unit,
    onMediaUriChange: (String) -> Unit,
    onMediaContextChange: (String) -> Unit,
    onCaptionChange: (String) -> Unit,
    onAudit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            onMediaUriChange(uri.toString())
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ForgePanel {
            SectionHeader("PR Risk Audit", trailing = if (state.auditing) "AUDITING" else "MEDIA + CAPTION")
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = state.creatorId,
                onValueChange = onCreatorIdChange,
                label = { Text("Creator ID") },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            PixelButton(
                label = "Upload image / video",
                onClick = { picker.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                active = state.mediaUri.isNotBlank(),
            )
            if (state.mediaUri.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                KeyValueLine("Selected", state.mediaUri.takeLast(34), valueColor = ForgeColor.Yellow)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.mediaContext,
                onValueChange = onMediaContextChange,
                label = { Text("What is in the media?") },
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.caption,
                onValueChange = onCaptionChange,
                label = { Text("Caption / post copy") },
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            PixelButton(
                label = if (state.auditing) "Auditing..." else "Audit before posting",
                onClick = onAudit,
                modifier = Modifier.fillMaxWidth(),
                active = state.auditing,
            )
            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                StatusPill(label = error.take(34), riskLevel = RiskLevel.High)
            }
        }

        ForgePanel {
            SectionHeader("Audit Report", trailing = if (state.report.isBlank()) "WAITING" else "READY")
            Spacer(modifier = Modifier.height(10.dp))
            if (state.report.isBlank()) {
                Text(
                    text = "Upload media or paste a caption to get brand-safety, PR, claim, and audience-fit feedback.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                AuditReportBlock(report = state.report)
            }
        }
    }
}

@Composable
private fun AuditReportBlock(report: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .border(1.dp, ForgeColor.Yellow.copy(alpha = 0.52f))
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        report.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { line ->
                val isHeader = line.isAuditHeader()
                Text(
                    text = line,
                    style = if (isHeader) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                    color = if (isHeader) ForgeColor.Yellow else ForgeColor.White,
                )
            }
    }
}

private fun String.isAuditHeader(): Boolean {
    val label = substringBefore(":").trim()
        .removePrefix("##")
        .removePrefix("#")
        .trim()
    if (label.length !in 3..40) return false
    return label.all { it.isUpperCase() || it.isWhitespace() || it == '_' || it == '-' }
}

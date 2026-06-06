package com.brandforge.app.presentation.lead

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brandforge.app.core.model.RiskLevel
import com.brandforge.app.domain.lead.AudienceInteractionType
import com.brandforge.app.domain.lead.Lead
import com.brandforge.app.domain.lead.LeadClassification
import com.brandforge.app.domain.lead.LeadPriority
import com.brandforge.app.ui.components.ForgePanel
import com.brandforge.app.ui.components.KeyValueLine
import com.brandforge.app.ui.components.PixelButton
import com.brandforge.app.ui.components.SectionHeader
import com.brandforge.app.ui.components.SignalBar
import com.brandforge.app.ui.components.StatusPill
import com.brandforge.app.ui.theme.ForgeColor
import kotlin.math.roundToInt

@Composable
fun LeadInboxScreen(
    state: LeadInboxUiState,
    onCreatorIdChange: (String) -> Unit,
    onSourceTypeChange: (AudienceInteractionType) -> Unit,
    onPlatformChange: (String) -> Unit,
    onAuthorHandleChange: (String) -> Unit,
    onInteractionTextChange: (String) -> Unit,
    onClassify: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ForgePanel {
            SectionHeader("Lead Detection Agent", trailing = if (state.classifying) "CLASSIFYING" else "FLASH LITE")
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = state.creatorId,
                onValueChange = onCreatorIdChange,
                label = { Text("Creator ID") },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AudienceInteractionType.entries.forEach { type ->
                    PixelButton(
                        label = type.shortLabel(),
                        onClick = { onSourceTypeChange(type) },
                        active = state.sourceType == type,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.platform,
                onValueChange = onPlatformChange,
                label = { Text("Platform") },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.authorHandle,
                onValueChange = onAuthorHandleChange,
                label = { Text("Author") },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.interactionText,
                onValueChange = onInteractionTextChange,
                label = { Text("Interaction") },
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            PixelButton(
                label = if (state.classifying) "Classifying..." else "Classify Interaction",
                onClick = onClassify,
                active = state.classifying,
                modifier = Modifier.fillMaxWidth(),
            )
            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                StatusPill(label = error.take(34), riskLevel = RiskLevel.High)
            }
        }

        ForgePanel {
            SectionHeader("Lead Inbox", trailing = "${state.leads.size} SAVED")
            Spacer(modifier = Modifier.height(10.dp))
            if (state.leads.isEmpty()) {
                Text(
                    text = "No classified audience interactions yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                state.leads.forEach { lead ->
                    LeadInboxItem(lead)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LeadInboxItem(lead: Lead) {
    val riskLevel = lead.riskLevel()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, riskLevel.color().copy(alpha = 0.72f))
            .padding(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusPill(label = lead.classification.label, riskLevel = riskLevel)
            StatusPill(label = lead.priority.label, riskLevel = riskLevel)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = lead.text,
            style = MaterialTheme.typography.titleMedium,
            color = ForgeColor.White,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        KeyValueLine("Source", "${lead.sourceType.label} / ${lead.platform}", valueColor = ForgeColor.Yellow)
        KeyValueLine("Author", lead.authorHandle.ifBlank { "Unknown" })
        KeyValueLine("Confidence", lead.confidence.percent(), valueColor = ForgeColor.Green)
        SignalBar(progress = lead.confidence, color = riskLevel.color())
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = lead.reason,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = lead.suggestedReply.ifBlank { "No reply recommended." },
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Green,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun AudienceInteractionType.shortLabel(): String =
    when (this) {
        AudienceInteractionType.Comment -> "Comment"
        AudienceInteractionType.DirectMessage -> "DM"
        AudienceInteractionType.AudienceInteraction -> "Signal"
    }

private fun Lead.riskLevel(): RiskLevel =
    when {
        classification == LeadClassification.PRRisk -> RiskLevel.High
        priority == LeadPriority.Critical -> RiskLevel.High
        priority == LeadPriority.High -> RiskLevel.High
        priority == LeadPriority.Medium -> RiskLevel.Medium
        else -> RiskLevel.Low
    }

private fun RiskLevel.color() =
    when (this) {
        RiskLevel.Low -> ForgeColor.Green
        RiskLevel.Medium -> ForgeColor.Yellow
        RiskLevel.High -> ForgeColor.Red
    }

private fun Float.percent(): String =
    "${(coerceIn(0f, 1f) * 100).roundToInt()}%"

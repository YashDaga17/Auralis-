package com.brandforge.app.presentation.trend

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brandforge.app.core.model.RiskLevel
import com.brandforge.app.domain.trend.TrendOpportunity
import com.brandforge.app.ui.components.ForgePanel
import com.brandforge.app.ui.components.KeyValueLine
import com.brandforge.app.ui.components.PixelButton
import com.brandforge.app.ui.components.SectionHeader
import com.brandforge.app.ui.components.SignalBar
import com.brandforge.app.ui.components.StatusPill
import com.brandforge.app.ui.theme.ForgeColor
import kotlin.math.roundToInt

@Composable
fun TrendRadarScreen(
    state: TrendRadarUiState,
    onCreatorIdChange: (String) -> Unit,
    onScan: () -> Unit,
    onSaveOpportunity: (String) -> Unit,
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
            SectionHeader("Trend Radar", trailing = if (state.scanning) "SCANNING" else "LIVE SOURCES")
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = state.creatorId,
                onValueChange = onCreatorIdChange,
                label = { Text("Creator ID") },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            PixelButton(
                label = if (state.scanning) "Scanning..." else "Fetch + Score Trends",
                onClick = onScan,
                modifier = Modifier.fillMaxWidth(),
                active = state.scanning,
            )
            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                StatusPill(label = error.take(34), riskLevel = RiskLevel.High)
            }
            state.saveMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                StatusPill(label = message.take(34), riskLevel = RiskLevel.Low)
            }
        }

        ForgePanel {
            SectionHeader("Opportunity Queue", trailing = "${state.opportunities.size} READY")
            Spacer(modifier = Modifier.height(8.dp))
            KeyValueLine(label = "Signals", value = state.signalCount.toString(), valueColor = ForgeColor.Green)
            Spacer(modifier = Modifier.height(10.dp))
            if (state.opportunities.isEmpty()) {
                Text(
                    text = "No opportunities persisted yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                state.opportunities.forEach { opportunity ->
                    TrendOpportunityItem(
                        opportunity = opportunity,
                        saved = opportunity.id in state.savedOpportunityIds,
                        onSave = { onSaveOpportunity(opportunity.id) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun TrendOpportunityItem(
    opportunity: TrendOpportunity,
    saved: Boolean,
    onSave: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForgeColor.White.copy(alpha = 0.34f))
            .padding(9.dp),
    ) {
        Text(
            text = opportunity.title,
            style = MaterialTheme.typography.titleMedium,
            color = ForgeColor.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = opportunity.sourcePlatform + " / " + opportunity.recommendedFormat,
            style = MaterialTheme.typography.labelMedium,
            color = ForgeColor.Yellow,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = opportunity.sourceUrl.ifBlank { "Source URL unavailable" },
            style = MaterialTheme.typography.bodySmall,
            color = ForgeColor.Green,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = opportunity.summary.ifBlank { opportunity.sourceUrl },
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PixelButton(
                label = "View source",
                onClick = {
                    val url = opportunity.sourceUrl
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        uriHandler.openUri(url)
                    }
                },
                modifier = Modifier.weight(1f),
                active = false,
            )
            PixelButton(
                label = if (saved) "Saved" else "Add to list",
                onClick = onSave,
                modifier = Modifier.weight(1f),
                active = saved,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        KeyValueLine(label = "Opportunity", value = opportunity.opportunityScore.percent(), valueColor = ForgeColor.Green)
        SignalBar(progress = opportunity.opportunityScore, color = ForgeColor.Green)
        Spacer(modifier = Modifier.height(6.dp))
        KeyValueLine(label = "Brand Fit", value = opportunity.brandFitScore.percent(), valueColor = ForgeColor.Yellow)
        KeyValueLine(label = "Velocity", value = opportunity.velocityScore.percent(), valueColor = ForgeColor.White)
        KeyValueLine(label = "Freshness", value = opportunity.freshnessScore.percent(), valueColor = ForgeColor.White)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = opportunity.rationale,
            style = MaterialTheme.typography.bodySmall,
            color = ForgeColor.Muted,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Float.percent(): String =
    "${(coerceIn(0f, 1f) * 100).roundToInt()}%"

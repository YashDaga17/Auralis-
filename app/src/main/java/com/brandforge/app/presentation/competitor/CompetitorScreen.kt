package com.brandforge.app.presentation.competitor

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
import com.brandforge.app.domain.competitor.Competitor
import com.brandforge.app.domain.competitor.CompetitorContent
import com.brandforge.app.domain.competitor.CompetitorInsight
import com.brandforge.app.ui.components.ForgePanel
import com.brandforge.app.ui.components.KeyValueLine
import com.brandforge.app.ui.components.PixelButton
import com.brandforge.app.ui.components.SectionHeader
import com.brandforge.app.ui.components.SignalBar
import com.brandforge.app.ui.components.StatusPill
import com.brandforge.app.ui.theme.ForgeColor
import kotlin.math.roundToInt

@Composable
fun CompetitorScreen(
    state: CompetitorUiState,
    onCreatorIdChange: (String) -> Unit,
    onCompetitorUrlChange: (String) -> Unit,
    onAnalyze: () -> Unit,
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
            SectionHeader("Competitor Intelligence", trailing = if (state.analyzing) "ANALYZING" else "GAP RADAR")
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = state.creatorId,
                onValueChange = onCreatorIdChange,
                label = { Text("Creator ID") },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.competitorUrl,
                onValueChange = onCompetitorUrlChange,
                label = { Text("Competitor URL / YouTube Channel / Website") },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            PixelButton(
                label = if (state.analyzing) "Analyzing..." else "Analyze Competitor",
                onClick = onAnalyze,
                active = state.analyzing,
                modifier = Modifier.fillMaxWidth(),
            )
            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                StatusPill(label = error.take(34), riskLevel = RiskLevel.High)
            }
        }

        ForgePanel {
            SectionHeader("Tracked Competitors", trailing = "${state.competitors.size} SOURCES")
            Spacer(modifier = Modifier.height(10.dp))
            if (state.competitors.isEmpty()) {
                Text(
                    text = "No competitor sources analyzed yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                state.competitors.forEach { competitor ->
                    CompetitorSourceItem(competitor)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        ForgePanel {
            SectionHeader("Fetched Content", trailing = "${state.content.size} ITEMS")
            Spacer(modifier = Modifier.height(10.dp))
            if (state.content.isEmpty()) {
                Text(
                    text = "No live competitor content has been fetched yet. Add a public YouTube channel URL or website page, then run analysis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                state.content.forEach { content ->
                    CompetitorContentItem(content)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        ForgePanel {
            SectionHeader("Strategic Gaps", trailing = "${state.insights.size} INSIGHTS")
            Spacer(modifier = Modifier.height(10.dp))
            if (state.insights.isEmpty()) {
                Text(
                    text = "No competitor gaps generated yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                state.insights.forEach { insight ->
                    CompetitorInsightItem(insight)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CompetitorContentItem(content: CompetitorContent) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForgeColor.White.copy(alpha = 0.34f))
            .padding(9.dp),
    ) {
        Text(
            text = content.title,
            style = MaterialTheme.typography.titleMedium,
            color = ForgeColor.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = content.summary.ifBlank { "No summary returned by source." },
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        KeyValueLine("Engagement", content.engagementEstimate.ifBlank { "unavailable" })
        Spacer(modifier = Modifier.height(7.dp))
        PixelButton(
            label = "View source",
            onClick = {
                if (content.sourceUrl.startsWith("http://") || content.sourceUrl.startsWith("https://")) {
                    uriHandler.openUri(content.sourceUrl)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CompetitorSourceItem(competitor: Competitor) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForgeColor.White.copy(alpha = 0.34f))
            .padding(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = competitor.name,
                style = MaterialTheme.typography.titleMedium,
                color = ForgeColor.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            StatusPill(label = competitor.platform.label)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = competitor.url,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        competitor.lastAnalyzed?.let {
            Spacer(modifier = Modifier.height(6.dp))
            KeyValueLine("Last analyzed", it.toString(), valueColor = ForgeColor.Green)
        }
    }
}

@Composable
private fun CompetitorInsightItem(insight: CompetitorInsight) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForgeColor.Yellow.copy(alpha = 0.65f))
            .padding(9.dp),
    ) {
        Text(
            text = insight.gap,
            style = MaterialTheme.typography.titleMedium,
            color = ForgeColor.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        KeyValueLine("Competitor Focus", insight.pattern, valueColor = ForgeColor.Yellow)
        KeyValueLine("Frequency", insight.frequency)
        KeyValueLine("Opportunity", insight.opportunityScore.percent(), valueColor = ForgeColor.Green)
        SignalBar(progress = insight.opportunityScore, color = ForgeColor.Green)
        Spacer(modifier = Modifier.height(6.dp))
        KeyValueLine("Suggested Format", insight.recommendedContentFormat, valueColor = ForgeColor.Yellow)
        Text(
            text = insight.recommendedHook,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Green,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = insight.recommendedAngle,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.White,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = insight.reasoning,
            style = MaterialTheme.typography.bodySmall,
            color = ForgeColor.Muted,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        KeyValueLine("Confidence", insight.confidence.percent(), valueColor = ForgeColor.Green)
    }
}

private fun Float.percent(): String =
    "${(coerceIn(0f, 1f) * 100).roundToInt()}%"

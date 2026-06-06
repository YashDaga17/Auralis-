package com.brandforge.app.presentation.warroom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.brandforge.app.core.model.BrandForgeState
import com.brandforge.app.core.model.RiskLevel
import com.brandforge.app.core.model.WorkflowCommand
import com.brandforge.app.ui.components.ForgePanel
import com.brandforge.app.ui.components.KeyValueLine
import com.brandforge.app.ui.components.PixelButton
import com.brandforge.app.ui.components.SectionHeader
import com.brandforge.app.ui.components.SignalBar
import com.brandforge.app.ui.components.StatusPill
import com.brandforge.app.ui.theme.ForgeColor

@Composable
fun WarRoomScreen(
    commandState: BrandForgeState,
    state: WarRoomUiState,
    onCreatorIdChange: (String) -> Unit,
    onBriefChange: (String) -> Unit,
    onRunBattle: () -> Unit,
    onPrepareWarRoom: () -> Unit,
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
            SectionHeader("War Room Caption Battle", trailing = if (state.runningBattle) "OPENROUTER" else "AGENTS READY")
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
                value = state.brief,
                onValueChange = onBriefChange,
                label = { Text("Caption battle brief") },
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            PixelButton(
                label = if (state.runningBattle) "Agents debating..." else "Run caption battle",
                onClick = onRunBattle,
                modifier = Modifier.fillMaxWidth(),
                active = state.runningBattle,
            )
            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                StatusPill(label = error.take(34), riskLevel = RiskLevel.High)
            }
        }

        ForgePanel(borderColor = ForgeColor.Green.copy(alpha = 0.72f)) {
            SectionHeader("Agent Battle Arena", trailing = if (state.runningBattle) "FIGHTING" else if (state.battleResult.isBlank()) "READY" else "SCORED")
            Spacer(modifier = Modifier.height(10.dp))
            AgentBattleArena(
                running = state.runningBattle,
                hasResult = state.battleResult.isNotBlank(),
            )
        }

        ForgePanel {
            SectionHeader("Battle Output", trailing = if (state.battleResult.isBlank()) "WAITING" else "WINNER")
            Spacer(modifier = Modifier.height(10.dp))
            if (state.battleResult.isBlank()) {
                Text(
                    text = "Describe the post/trend/product and let Brand DNA, Virality, Competitor, and Supervisor agents fight for the strongest caption.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                Text(
                    text = state.battleResult,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.White,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ForgeColor.Yellow.copy(alpha = 0.52f))
                        .padding(9.dp),
                )
            }
        }

        ForgePanel {
            SectionHeader("Office Kit", trailing = commandState.twin.officeKitMode.name.uppercase())
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Phone remains canonical. Laptop War Room receives content, trends, reports, and competitor analysis handoffs.",
                style = MaterialTheme.typography.bodyMedium,
                color = ForgeColor.Muted,
            )
            Spacer(modifier = Modifier.height(10.dp))
            KeyValueLine("Source", commandState.twin.sourceOfTruth, valueColor = ForgeColor.Yellow)
            KeyValueLine("Handoff", "content / trends / reports / competitors")
            Spacer(modifier = Modifier.height(10.dp))
            PixelButton(
                label = "Prepare War Room handoff",
                onClick = onPrepareWarRoom,
                modifier = Modifier.fillMaxWidth(),
                active = commandState.activeWorkflow == WorkflowCommand.PrepareWarRoom,
            )
        }

        ForgePanel {
            SectionHeader("Strategy Brief", trailing = "READY")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = commandState.strategyBrief.title, style = MaterialTheme.typography.titleMedium, color = ForgeColor.White)
            Spacer(modifier = Modifier.height(8.dp))
            KeyValueLine("Morning", commandState.strategyBrief.morningBriefing)
            KeyValueLine("Weekly", commandState.strategyBrief.weeklyMove)
            KeyValueLine("PR watch", commandState.strategyBrief.prWatch, valueColor = ForgeColor.Yellow)
            KeyValueLine("Next", commandState.strategyBrief.nextBestAction, valueColor = ForgeColor.Green)
        }
    }
}

@Composable
private fun AgentBattleArena(
    running: Boolean,
    hasResult: Boolean,
) {
    val agents = listOf(
        BattleAgent("DNA", "Brand DNA", "Voice guard", ForgeColor.Yellow, if (hasResult) 0.91f else if (running) 0.72f else 0.2f),
        BattleAgent("VIR", "Virality", "Hook force", ForgeColor.Green, if (hasResult) 0.88f else if (running) 0.82f else 0.2f),
        BattleAgent("CMP", "Competitor", "Gap angle", ForgeColor.White, if (hasResult) 0.79f else if (running) 0.66f else 0.2f),
        BattleAgent("SUP", "Supervisor", "Final call", ForgeColor.Green, if (hasResult) 0.94f else if (running) 0.58f else 0.2f),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BattleAgentNode(agent = agents[0], running = running, modifier = Modifier.weight(1f))
            BattleVsNode(modifier = Modifier.weight(0.34f))
            BattleAgentNode(agent = agents[1], running = running, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BattleAgentNode(agent = agents[2], running = running, modifier = Modifier.weight(1f))
            BattleVsNode(modifier = Modifier.weight(0.34f))
            BattleAgentNode(agent = agents[3], running = running, modifier = Modifier.weight(1f))
        }
        agents.forEach { agent ->
            KeyValueLine(
                label = agent.name,
                value = if (running) "debating" else if (hasResult) "${(agent.score * 100).toInt()}%" else "queued",
                valueColor = agent.color,
            )
            SignalBar(progress = agent.score, color = agent.color, height = 6.dp)
        }
    }
}

@Composable
private fun BattleAgentNode(
    agent: BattleAgent,
    running: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(1.dp, agent.color.copy(alpha = 0.72f))
            .background(agent.color.copy(alpha = if (running) 0.14f else 0.07f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        PixelAgentAvatar(color = agent.color)
        Text(
            text = agent.callSign,
            style = MaterialTheme.typography.titleMedium,
            color = agent.color,
            maxLines = 1,
        )
        Text(
            text = agent.role,
            style = MaterialTheme.typography.bodySmall,
            color = ForgeColor.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BattleVsNode(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(96.dp)
            .border(1.dp, ForgeColor.Red.copy(alpha = 0.58f))
            .background(ForgeColor.Red.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "VS",
            style = MaterialTheme.typography.titleMedium,
            color = ForgeColor.Red,
        )
    }
}

@Composable
private fun PixelAgentAvatar(color: Color) {
    Column(
        modifier = Modifier
            .border(1.dp, color)
            .background(ForgeColor.Black)
            .padding(4.dp),
    ) {
        repeat(5) { row ->
            Row {
                repeat(5) { col ->
                    val filled = when (row) {
                        0 -> col in 1..3
                        1 -> col == 0 || col == 2 || col == 4
                        2 -> col in 0..4
                        3 -> col == 1 || col == 3
                        else -> col in 0..4
                    }
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(if (filled) color else Color.Transparent),
                    )
                }
            }
        }
    }
}

private data class BattleAgent(
    val callSign: String,
    val name: String,
    val role: String,
    val color: Color,
    val score: Float,
)

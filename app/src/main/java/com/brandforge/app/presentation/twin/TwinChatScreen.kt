package com.brandforge.app.presentation.twin

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.brandforge.app.domain.twin.TwinChatMessage
import com.brandforge.app.domain.twin.TwinChatRole
import com.brandforge.app.ui.components.ForgePanel
import com.brandforge.app.ui.components.KeyValueLine
import com.brandforge.app.ui.components.PixelButton
import com.brandforge.app.ui.components.SectionHeader
import com.brandforge.app.ui.components.StatusPill
import com.brandforge.app.ui.theme.ForgeColor

@Composable
fun TwinChatScreen(
    state: TwinChatUiState,
    onCreatorIdChange: (String) -> Unit,
    onDraftMessageChange: (String) -> Unit,
    onSend: () -> Unit,
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
            SectionHeader("Digital Twin Chat", trailing = if (state.sending) "THINKING" else "STRATEGIST")
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
                value = state.draftMessage,
                onValueChange = onDraftMessageChange,
                label = { Text("Ask your AI Twin") },
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            PixelButton(
                label = if (state.sending) "Thinking..." else "Ask Twin",
                onClick = onSend,
                modifier = Modifier.fillMaxWidth(),
                active = state.sending,
            )
            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                StatusPill(label = error.take(34), riskLevel = RiskLevel.High)
            }
        }

        ForgePanel {
            SectionHeader("Conversation", trailing = "${state.messages.size} MSG")
            Spacer(modifier = Modifier.height(8.dp))
            if (state.messages.isEmpty()) {
                Text(
                    text = "No local Twin conversation yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                state.messages.forEach { message ->
                    TwinMessageItem(message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun TwinMessageItem(message: TwinChatMessage) {
    val isTwin = message.role == TwinChatRole.Twin
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isTwin) ForgeColor.Yellow else ForgeColor.White.copy(alpha = 0.42f),
            )
            .padding(9.dp),
    ) {
        Text(
            text = if (isTwin) "DIGITAL TWIN" else "CREATOR",
            style = MaterialTheme.typography.labelMedium,
            color = if (isTwin) ForgeColor.Yellow else ForgeColor.White,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = message.message,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.White,
        )
        if (isTwin) {
            Spacer(modifier = Modifier.height(8.dp))
            KeyValueLine("Memories", message.memoryIds.joinToString().ifBlank { "none" }, valueColor = ForgeColor.Green)
            KeyValueLine("Trends", message.trendIds.joinToString().ifBlank { "none" }, valueColor = ForgeColor.Yellow)
            KeyValueLine("Opportunities", message.opportunityIds.joinToString().ifBlank { "none" }, valueColor = ForgeColor.Yellow)
            KeyValueLine("Drafts", message.contentDraftIds.joinToString().ifBlank { "none" }, valueColor = ForgeColor.Green)
        }
    }
}

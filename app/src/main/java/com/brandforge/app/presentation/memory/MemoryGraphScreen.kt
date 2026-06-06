package com.brandforge.app.presentation.memory

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
import com.brandforge.app.domain.memory.MemoryShard
import com.brandforge.app.ui.components.ForgePanel
import com.brandforge.app.ui.components.PixelButton
import com.brandforge.app.ui.components.SectionHeader
import com.brandforge.app.ui.components.SignalBar
import com.brandforge.app.ui.components.StatusPill
import com.brandforge.app.ui.theme.ForgeColor

@Composable
fun MemoryGraphScreen(
    state: MemoryUiState,
    onCreatorIdChange: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onRetrieve: () -> Unit,
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
            SectionHeader("Creator Memory Layer", trailing = "ROOM + QDRANT")
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
                value = state.query,
                onValueChange = onQueryChange,
                label = { Text("Retrieval Query") },
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            PixelButton(
                label = if (state.loading) "Retrieving..." else "Retrieve memory",
                onClick = onRetrieve,
                modifier = Modifier.fillMaxWidth(),
            )
            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                StatusPill(label = error.take(32))
            }
        }

        ForgePanel {
            SectionHeader("Retrieved Memories", trailing = "${state.results.size} MATCHES")
            Spacer(modifier = Modifier.height(10.dp))
            if (state.results.isEmpty()) {
                Text(
                    text = "No creator memories retrieved yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                )
            } else {
                state.results.forEach { memory ->
                    MemoryResultItem(memory)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MemoryResultItem(memory: MemoryShard) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForgeColor.White.copy(alpha = 0.34f))
            .padding(9.dp),
    ) {
        Text(
            text = "${memory.type.name} / ${memory.title}",
            style = MaterialTheme.typography.titleMedium,
            color = ForgeColor.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = memory.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SignalBar(progress = memory.retrievalWeight, color = ForgeColor.Green)
    }
}

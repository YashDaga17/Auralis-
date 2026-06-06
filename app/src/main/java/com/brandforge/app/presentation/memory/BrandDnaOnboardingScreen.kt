package com.brandforge.app.presentation.memory

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
import com.brandforge.app.ui.components.ForgePanel
import com.brandforge.app.ui.components.PixelButton
import com.brandforge.app.ui.components.SectionHeader
import com.brandforge.app.ui.components.StatusPill

@Composable
fun BrandDnaOnboardingScreen(
    state: BrandDnaUiState,
    onCreatorIdChange: (String) -> Unit,
    onProfileUrlChange: (String) -> Unit,
    onIngestProfile: () -> Unit,
    onCreatorNameChange: (String) -> Unit,
    onArchetypeChange: (String) -> Unit,
    onVoiceRulesChange: (String) -> Unit,
    onBannedClaimsChange: (String) -> Unit,
    onBusinessGoalsChange: (String) -> Unit,
    onSave: () -> Unit,
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
            SectionHeader("Brand DNA Onboarding", trailing = if (state.saveCompleted) "SAVED" else "LOCAL FIRST")
            Spacer(modifier = Modifier.height(10.dp))
            BrandTextField("Creator ID", state.creatorId, onCreatorIdChange)
            BrandTextField("Profile URL", state.profileUrl, onProfileUrlChange)
            PixelButton(
                label = when {
                    state.ingestingProfile -> "Scraping profile..."
                    state.profileIngestCompleted -> "Profile learned"
                    else -> "Scrape profile + build DNA"
                },
                onClick = onIngestProfile,
                modifier = Modifier.fillMaxWidth(),
                active = state.profileIngestCompleted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            BrandTextField("Creator Name", state.creatorName, onCreatorNameChange)
            BrandTextField("Archetype", state.archetype, onArchetypeChange)
            BrandTextField("Voice Rules JSON", state.voiceRulesJson, onVoiceRulesChange, minLines = 4)
            BrandTextField("Banned Claims JSON", state.bannedClaimsJson, onBannedClaimsChange, minLines = 3)
            BrandTextField("Business Goals JSON", state.businessGoalsJson, onBusinessGoalsChange, minLines = 3)
            Spacer(modifier = Modifier.height(8.dp))
            PixelButton(
                label = if (state.saving) "Saving..." else "Persist Brand DNA",
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                active = state.saveCompleted,
            )
            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                StatusPill(label = error.take(32))
            }
        }
    }
}

@Composable
private fun BrandTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        minLines = minLines,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    )
}

package com.brandforge.app.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.brandforge.app.core.model.AgentLog
import com.brandforge.app.core.model.AgentMode
import com.brandforge.app.core.model.AgentState
import com.brandforge.app.core.model.BrandForgeState
import com.brandforge.app.core.model.ContentQueueItem
import com.brandforge.app.core.model.CreatorMetric
import com.brandforge.app.core.model.RiskLevel
import com.brandforge.app.core.model.TrendOpportunity
import com.brandforge.app.core.model.WorkflowCommand
import com.brandforge.app.core.debug.DebugChecklistStatus
import com.brandforge.app.domain.content.ContentFormat
import com.brandforge.app.domain.content.MediaArtifactType
import com.brandforge.app.domain.lead.AudienceInteractionType
import com.brandforge.app.presentation.commandcenter.CommandCenterViewModel
import com.brandforge.app.presentation.competitor.CompetitorScreen
import com.brandforge.app.presentation.competitor.CompetitorUiState
import com.brandforge.app.presentation.competitor.CompetitorViewModel
import com.brandforge.app.presentation.content.ContentStudioScreen
import com.brandforge.app.presentation.content.ContentStudioUiState
import com.brandforge.app.presentation.content.ContentStudioViewModel
import com.brandforge.app.presentation.debug.DebugPanelScreen
import com.brandforge.app.presentation.debug.DebugUiState
import com.brandforge.app.presentation.debug.DebugViewModel
import com.brandforge.app.presentation.lead.LeadInboxScreen
import com.brandforge.app.presentation.lead.LeadInboxUiState
import com.brandforge.app.presentation.lead.LeadInboxViewModel
import com.brandforge.app.presentation.memory.BrandDnaOnboardingScreen
import com.brandforge.app.presentation.memory.BrandDnaUiState
import com.brandforge.app.presentation.memory.BrandDnaViewModel
import com.brandforge.app.presentation.memory.MemoryGraphScreen
import com.brandforge.app.presentation.memory.MemoryUiState
import com.brandforge.app.presentation.memory.MemoryViewModel
import com.brandforge.app.presentation.navigation.BrandForgeDestination
import com.brandforge.app.presentation.pr.PrRiskAuditScreen
import com.brandforge.app.presentation.pr.PrRiskAuditUiState
import com.brandforge.app.presentation.pr.PrRiskAuditViewModel
import com.brandforge.app.presentation.trend.TrendRadarScreen
import com.brandforge.app.presentation.trend.TrendRadarUiState
import com.brandforge.app.presentation.trend.TrendRadarViewModel
import com.brandforge.app.presentation.twin.TwinChatScreen as RealTwinChatScreen
import com.brandforge.app.presentation.twin.TwinChatUiState
import com.brandforge.app.presentation.twin.TwinChatViewModel
import com.brandforge.app.presentation.voice.AndroidSpeechCommandRecognizer
import com.brandforge.app.presentation.warroom.WarRoomScreen
import com.brandforge.app.presentation.warroom.WarRoomUiState
import com.brandforge.app.presentation.warroom.WarRoomViewModel
import com.brandforge.app.ui.components.AnimatedBrandLogo
import com.brandforge.app.ui.components.ForgePanel
import com.brandforge.app.ui.components.GridBackdrop
import com.brandforge.app.ui.components.KeyValueLine
import com.brandforge.app.ui.components.PixelButton
import com.brandforge.app.ui.components.PixelTwinAvatar
import com.brandforge.app.ui.components.SectionHeader
import com.brandforge.app.ui.components.SignalBar
import com.brandforge.app.ui.components.StatusPill
import com.brandforge.app.ui.components.TerminalDivider
import com.brandforge.app.ui.theme.ForgeColor

@Composable
fun BrandForgeApp(
    viewModel: CommandCenterViewModel,
    brandDnaViewModel: BrandDnaViewModel,
    memoryViewModel: MemoryViewModel,
    trendRadarViewModel: TrendRadarViewModel,
    contentStudioViewModel: ContentStudioViewModel,
    twinChatViewModel: TwinChatViewModel,
    leadInboxViewModel: LeadInboxViewModel,
    competitorViewModel: CompetitorViewModel,
    debugViewModel: DebugViewModel,
    prRiskAuditViewModel: PrRiskAuditViewModel,
    warRoomViewModel: WarRoomViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    val selectedDestination by viewModel.selectedDestination.collectAsState()
    val brandDnaState by brandDnaViewModel.uiState.collectAsState()
    val memoryState by memoryViewModel.uiState.collectAsState()
    val trendRadarState by trendRadarViewModel.uiState.collectAsState()
    val contentStudioState by contentStudioViewModel.uiState.collectAsState()
    val twinChatState by twinChatViewModel.uiState.collectAsState()
    val leadInboxState by leadInboxViewModel.uiState.collectAsState()
    val competitorState by competitorViewModel.uiState.collectAsState()
    val prRiskAuditState by prRiskAuditViewModel.uiState.collectAsState()
    val warRoomState by warRoomViewModel.uiState.collectAsState()
    val debugState by debugViewModel.uiState.collectAsState()
    var debugTapCount by remember { mutableStateOf(0) }
    var showDebugPanel by remember { mutableStateOf(false) }
    val onLogoTap = {
        debugTapCount += 1
        if (debugTapCount >= 5) {
            showDebugPanel = true
            debugTapCount = 0
        }
    }
    val debugActions = DebugActions.from(debugViewModel)
    val prRiskAuditActions = PrRiskAuditActions.from(prRiskAuditViewModel)
    val warRoomActions = WarRoomActions.from(warRoomViewModel)

    Box(modifier = Modifier.fillMaxSize()) {
        GridBackdrop(modifier = Modifier.fillMaxSize())
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            if (maxWidth >= 840.dp) {
                WarRoomShell(
                    state = state,
                    selectedDestination = selectedDestination,
                    onDestinationSelected = viewModel::selectDestination,
                    onWorkflow = viewModel::runWorkflow,
                    onVoiceTranscript = viewModel::routeVoiceCommand,
                    brandDnaState = brandDnaState,
                    memoryState = memoryState,
                    trendRadarState = trendRadarState,
                    contentStudioState = contentStudioState,
                    twinChatState = twinChatState,
                    leadInboxState = leadInboxState,
                    competitorState = competitorState,
                    prRiskAuditState = prRiskAuditState,
                    warRoomState = warRoomState,
                    debugState = debugState,
                    showDebugPanel = showDebugPanel,
                    brandDnaActions = BrandDnaActions.from(brandDnaViewModel),
                    memoryActions = MemoryActions.from(memoryViewModel),
                    trendRadarActions = TrendRadarActions.from(trendRadarViewModel),
                    contentStudioActions = ContentStudioActions.from(contentStudioViewModel),
                    twinChatActions = TwinChatActions.from(twinChatViewModel),
                    leadInboxActions = LeadInboxActions.from(leadInboxViewModel),
                    competitorActions = CompetitorActions.from(competitorViewModel),
                    prRiskAuditActions = prRiskAuditActions,
                    warRoomActions = warRoomActions,
                    debugActions = debugActions,
                    onLogoTap = onLogoTap,
                    onCloseDebug = { showDebugPanel = false },
                )
            } else {
                PhoneShell(
                    state = state,
                    selectedDestination = selectedDestination,
                    onDestinationSelected = viewModel::selectDestination,
                    onWorkflow = viewModel::runWorkflow,
                    onVoiceTranscript = viewModel::routeVoiceCommand,
                    brandDnaState = brandDnaState,
                    memoryState = memoryState,
                    trendRadarState = trendRadarState,
                    contentStudioState = contentStudioState,
                    twinChatState = twinChatState,
                    leadInboxState = leadInboxState,
                    competitorState = competitorState,
                    prRiskAuditState = prRiskAuditState,
                    warRoomState = warRoomState,
                    debugState = debugState,
                    showDebugPanel = showDebugPanel,
                    brandDnaActions = BrandDnaActions.from(brandDnaViewModel),
                    memoryActions = MemoryActions.from(memoryViewModel),
                    trendRadarActions = TrendRadarActions.from(trendRadarViewModel),
                    contentStudioActions = ContentStudioActions.from(contentStudioViewModel),
                    twinChatActions = TwinChatActions.from(twinChatViewModel),
                    leadInboxActions = LeadInboxActions.from(leadInboxViewModel),
                    competitorActions = CompetitorActions.from(competitorViewModel),
                    prRiskAuditActions = prRiskAuditActions,
                    warRoomActions = warRoomActions,
                    debugActions = debugActions,
                    onLogoTap = onLogoTap,
                    onCloseDebug = { showDebugPanel = false },
                )
            }
        }
    }
}

private data class BrandDnaActions(
    val onCreatorIdChange: (String) -> Unit,
    val onProfileUrlChange: (String) -> Unit,
    val onIngestProfile: () -> Unit,
    val onCreatorNameChange: (String) -> Unit,
    val onArchetypeChange: (String) -> Unit,
    val onVoiceRulesChange: (String) -> Unit,
    val onBannedClaimsChange: (String) -> Unit,
    val onBusinessGoalsChange: (String) -> Unit,
    val onSave: () -> Unit,
) {
    companion object {
        fun from(viewModel: BrandDnaViewModel): BrandDnaActions =
            BrandDnaActions(
                onCreatorIdChange = viewModel::updateCreatorId,
                onProfileUrlChange = viewModel::updateProfileUrl,
                onIngestProfile = viewModel::ingestProfile,
                onCreatorNameChange = viewModel::updateCreatorName,
                onArchetypeChange = viewModel::updateArchetype,
                onVoiceRulesChange = viewModel::updateVoiceRules,
                onBannedClaimsChange = viewModel::updateBannedClaims,
                onBusinessGoalsChange = viewModel::updateBusinessGoals,
                onSave = viewModel::save,
            )
    }
}

private data class MemoryActions(
    val onCreatorIdChange: (String) -> Unit,
    val onQueryChange: (String) -> Unit,
    val onRetrieve: () -> Unit,
) {
    companion object {
        fun from(viewModel: MemoryViewModel): MemoryActions =
            MemoryActions(
                onCreatorIdChange = viewModel::updateCreatorId,
                onQueryChange = viewModel::updateQuery,
                onRetrieve = viewModel::retrieve,
            )
    }
}

private data class TrendRadarActions(
    val onCreatorIdChange: (String) -> Unit,
    val onScan: () -> Unit,
    val onSaveOpportunity: (String) -> Unit,
) {
    companion object {
        fun from(viewModel: TrendRadarViewModel): TrendRadarActions =
            TrendRadarActions(
                onCreatorIdChange = viewModel::updateCreatorId,
                onScan = viewModel::scan,
                onSaveOpportunity = viewModel::saveOpportunity,
            )
    }
}

private data class ContentStudioActions(
    val onCreatorIdChange: (String) -> Unit,
    val onGenerate: (String, ContentFormat) -> Unit,
    val onMediaPromptChange: (String) -> Unit,
    val onGenerateMedia: (MediaArtifactType) -> Unit,
) {
    companion object {
        fun from(viewModel: ContentStudioViewModel): ContentStudioActions =
            ContentStudioActions(
                onCreatorIdChange = viewModel::updateCreatorId,
                onGenerate = viewModel::generate,
                onMediaPromptChange = viewModel::updateMediaPrompt,
                onGenerateMedia = viewModel::generateMedia,
            )
    }
}

private data class TwinChatActions(
    val onCreatorIdChange: (String) -> Unit,
    val onDraftMessageChange: (String) -> Unit,
    val onSend: () -> Unit,
) {
    companion object {
        fun from(viewModel: TwinChatViewModel): TwinChatActions =
            TwinChatActions(
                onCreatorIdChange = viewModel::updateCreatorId,
                onDraftMessageChange = viewModel::updateDraftMessage,
                onSend = viewModel::send,
            )
    }
}

private data class LeadInboxActions(
    val onCreatorIdChange: (String) -> Unit,
    val onSourceTypeChange: (AudienceInteractionType) -> Unit,
    val onPlatformChange: (String) -> Unit,
    val onAuthorHandleChange: (String) -> Unit,
    val onInteractionTextChange: (String) -> Unit,
    val onClassify: () -> Unit,
) {
    companion object {
        fun from(viewModel: LeadInboxViewModel): LeadInboxActions =
            LeadInboxActions(
                onCreatorIdChange = viewModel::updateCreatorId,
                onSourceTypeChange = viewModel::updateSourceType,
                onPlatformChange = viewModel::updatePlatform,
                onAuthorHandleChange = viewModel::updateAuthorHandle,
                onInteractionTextChange = viewModel::updateInteractionText,
                onClassify = viewModel::classify,
            )
    }
}

private data class CompetitorActions(
    val onCreatorIdChange: (String) -> Unit,
    val onCompetitorUrlChange: (String) -> Unit,
    val onAnalyze: () -> Unit,
) {
    companion object {
        fun from(viewModel: CompetitorViewModel): CompetitorActions =
            CompetitorActions(
                onCreatorIdChange = viewModel::updateCreatorId,
                onCompetitorUrlChange = viewModel::updateCompetitorUrl,
                onAnalyze = viewModel::analyze,
            )
    }
}

private data class PrRiskAuditActions(
    val onCreatorIdChange: (String) -> Unit,
    val onMediaUriChange: (String) -> Unit,
    val onMediaContextChange: (String) -> Unit,
    val onCaptionChange: (String) -> Unit,
    val onAudit: () -> Unit,
) {
    companion object {
        fun from(viewModel: PrRiskAuditViewModel): PrRiskAuditActions =
            PrRiskAuditActions(
                onCreatorIdChange = viewModel::updateCreatorId,
                onMediaUriChange = viewModel::updateMediaUri,
                onMediaContextChange = viewModel::updateMediaContext,
                onCaptionChange = viewModel::updateCaption,
                onAudit = viewModel::audit,
            )
    }
}

private data class WarRoomActions(
    val onCreatorIdChange: (String) -> Unit,
    val onBriefChange: (String) -> Unit,
    val onRunBattle: () -> Unit,
) {
    companion object {
        fun from(viewModel: WarRoomViewModel): WarRoomActions =
            WarRoomActions(
                onCreatorIdChange = viewModel::updateCreatorId,
                onBriefChange = viewModel::updateBrief,
                onRunBattle = viewModel::runCaptionBattle,
            )
    }
}

private data class DebugActions(
    val onRunHealthCheck: () -> Unit,
    val onSeedDebugData: () -> Unit,
    val onChecklistStatusChange: (String, DebugChecklistStatus) -> Unit,
    val onClearErrors: () -> Unit,
) {
    companion object {
        fun from(viewModel: DebugViewModel): DebugActions =
            DebugActions(
                onRunHealthCheck = viewModel::runHealthCheck,
                onSeedDebugData = viewModel::seedDebugData,
                onChecklistStatusChange = viewModel::updateChecklistStatus,
                onClearErrors = viewModel::clearErrors,
            )
    }
}

@Composable
private fun PhoneShell(
    state: BrandForgeState,
    selectedDestination: BrandForgeDestination,
    onDestinationSelected: (BrandForgeDestination) -> Unit,
    onWorkflow: (WorkflowCommand) -> Unit,
    onVoiceTranscript: (String) -> Unit,
    brandDnaState: BrandDnaUiState,
    memoryState: MemoryUiState,
    trendRadarState: TrendRadarUiState,
    contentStudioState: ContentStudioUiState,
    twinChatState: TwinChatUiState,
    leadInboxState: LeadInboxUiState,
    competitorState: CompetitorUiState,
    prRiskAuditState: PrRiskAuditUiState,
    warRoomState: WarRoomUiState,
    debugState: DebugUiState,
    showDebugPanel: Boolean,
    brandDnaActions: BrandDnaActions,
    memoryActions: MemoryActions,
    trendRadarActions: TrendRadarActions,
    contentStudioActions: ContentStudioActions,
    twinChatActions: TwinChatActions,
    leadInboxActions: LeadInboxActions,
    competitorActions: CompetitorActions,
    prRiskAuditActions: PrRiskAuditActions,
    warRoomActions: WarRoomActions,
    debugActions: DebugActions,
    onLogoTap: () -> Unit,
    onCloseDebug: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        BrandHeader(state = state, compact = true, onLogoTap = onLogoTap)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            DestinationContent(
                destination = selectedDestination,
                state = state,
                onWorkflow = onWorkflow,
                onVoiceTranscript = onVoiceTranscript,
                brandDnaState = brandDnaState,
                memoryState = memoryState,
                trendRadarState = trendRadarState,
                contentStudioState = contentStudioState,
                twinChatState = twinChatState,
                leadInboxState = leadInboxState,
                competitorState = competitorState,
                prRiskAuditState = prRiskAuditState,
                warRoomState = warRoomState,
                debugState = debugState,
                showDebugPanel = showDebugPanel,
                brandDnaActions = brandDnaActions,
                memoryActions = memoryActions,
                trendRadarActions = trendRadarActions,
                contentStudioActions = contentStudioActions,
                twinChatActions = twinChatActions,
                leadInboxActions = leadInboxActions,
                competitorActions = competitorActions,
                prRiskAuditActions = prRiskAuditActions,
                warRoomActions = warRoomActions,
                debugActions = debugActions,
                onCloseDebug = onCloseDebug,
                modifier = Modifier.fillMaxSize(),
            )
        }
        TerminalNavigation(
            selectedDestination = selectedDestination,
            onDestinationSelected = onDestinationSelected,
        )
    }
}

@Composable
private fun WarRoomShell(
    state: BrandForgeState,
    selectedDestination: BrandForgeDestination,
    onDestinationSelected: (BrandForgeDestination) -> Unit,
    onWorkflow: (WorkflowCommand) -> Unit,
    onVoiceTranscript: (String) -> Unit,
    brandDnaState: BrandDnaUiState,
    memoryState: MemoryUiState,
    trendRadarState: TrendRadarUiState,
    contentStudioState: ContentStudioUiState,
    twinChatState: TwinChatUiState,
    leadInboxState: LeadInboxUiState,
    competitorState: CompetitorUiState,
    prRiskAuditState: PrRiskAuditUiState,
    warRoomState: WarRoomUiState,
    debugState: DebugUiState,
    showDebugPanel: Boolean,
    brandDnaActions: BrandDnaActions,
    memoryActions: MemoryActions,
    trendRadarActions: TrendRadarActions,
    contentStudioActions: ContentStudioActions,
    twinChatActions: TwinChatActions,
    leadInboxActions: LeadInboxActions,
    competitorActions: CompetitorActions,
    prRiskAuditActions: PrRiskAuditActions,
    warRoomActions: WarRoomActions,
    debugActions: DebugActions,
    onLogoTap: () -> Unit,
    onCloseDebug: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier
                .width(168.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AnimatedBrandLogo(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = onLogoTap),
                size = 72.dp,
            )
            Text(
                text = "BRANDFORGE",
                style = MaterialTheme.typography.headlineMedium,
                color = ForgeColor.Yellow,
                maxLines = 2,
                modifier = Modifier.clickable(onClick = onLogoTap),
            )
            Text(
                text = "PHONE SOURCE OF TRUTH",
                style = MaterialTheme.typography.labelMedium,
                color = ForgeColor.Green,
            )
            BrandForgeDestination.entries.forEach { destination ->
                PixelButton(
                    label = destination.shortLabel + " / " + destination.label,
                    active = selectedDestination == destination,
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            BrandHeader(state = state, compact = false, onLogoTap = onLogoTap)
            DestinationContent(
                destination = selectedDestination,
                state = state,
                onWorkflow = onWorkflow,
                onVoiceTranscript = onVoiceTranscript,
                brandDnaState = brandDnaState,
                memoryState = memoryState,
                trendRadarState = trendRadarState,
                contentStudioState = contentStudioState,
                twinChatState = twinChatState,
                leadInboxState = leadInboxState,
                competitorState = competitorState,
                prRiskAuditState = prRiskAuditState,
                warRoomState = warRoomState,
                debugState = debugState,
                showDebugPanel = showDebugPanel,
                brandDnaActions = brandDnaActions,
                memoryActions = memoryActions,
                trendRadarActions = trendRadarActions,
                contentStudioActions = contentStudioActions,
                twinChatActions = twinChatActions,
                leadInboxActions = leadInboxActions,
                competitorActions = competitorActions,
                prRiskAuditActions = prRiskAuditActions,
                warRoomActions = warRoomActions,
                debugActions = debugActions,
                onCloseDebug = onCloseDebug,
                modifier = Modifier.weight(1f),
            )
        }
        Column(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AgentPulsePanel(state.agents.take(5))
            LiveLogsPanel(logs = state.logs, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DestinationContent(
    destination: BrandForgeDestination,
    state: BrandForgeState,
    onWorkflow: (WorkflowCommand) -> Unit,
    onVoiceTranscript: (String) -> Unit,
    brandDnaState: BrandDnaUiState,
    memoryState: MemoryUiState,
    trendRadarState: TrendRadarUiState,
    contentStudioState: ContentStudioUiState,
    twinChatState: TwinChatUiState,
    leadInboxState: LeadInboxUiState,
    competitorState: CompetitorUiState,
    prRiskAuditState: PrRiskAuditUiState,
    warRoomState: WarRoomUiState,
    debugState: DebugUiState,
    showDebugPanel: Boolean,
    brandDnaActions: BrandDnaActions,
    memoryActions: MemoryActions,
    trendRadarActions: TrendRadarActions,
    contentStudioActions: ContentStudioActions,
    twinChatActions: TwinChatActions,
    leadInboxActions: LeadInboxActions,
    competitorActions: CompetitorActions,
    prRiskAuditActions: PrRiskAuditActions,
    warRoomActions: WarRoomActions,
    debugActions: DebugActions,
    onCloseDebug: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (showDebugPanel) {
        DebugPanelScreen(
            state = debugState,
            onRunHealthCheck = debugActions.onRunHealthCheck,
            onSeedDebugData = debugActions.onSeedDebugData,
            onChecklistStatusChange = debugActions.onChecklistStatusChange,
            onClearErrors = debugActions.onClearErrors,
            onClose = onCloseDebug,
            modifier = modifier,
        )
        return
    }

    when (destination) {
        BrandForgeDestination.BrandDnaSetup -> BrandDnaOnboardingScreen(
            state = brandDnaState,
            onCreatorIdChange = brandDnaActions.onCreatorIdChange,
            onProfileUrlChange = brandDnaActions.onProfileUrlChange,
            onIngestProfile = brandDnaActions.onIngestProfile,
            onCreatorNameChange = brandDnaActions.onCreatorNameChange,
            onArchetypeChange = brandDnaActions.onArchetypeChange,
            onVoiceRulesChange = brandDnaActions.onVoiceRulesChange,
            onBannedClaimsChange = brandDnaActions.onBannedClaimsChange,
            onBusinessGoalsChange = brandDnaActions.onBusinessGoalsChange,
            onSave = brandDnaActions.onSave,
            modifier = modifier,
        )
        BrandForgeDestination.Command -> CommandCenterScreen(state, onWorkflow, onVoiceTranscript, modifier)
        BrandForgeDestination.Agents -> AgentsScreen(state, onWorkflow, modifier)
        BrandForgeDestination.Memory -> MemoryGraphScreen(
            state = memoryState,
            onCreatorIdChange = memoryActions.onCreatorIdChange,
            onQueryChange = memoryActions.onQueryChange,
            onRetrieve = memoryActions.onRetrieve,
            modifier = modifier,
        )
        BrandForgeDestination.Trends -> TrendRadarScreen(
            state = trendRadarState,
            onCreatorIdChange = trendRadarActions.onCreatorIdChange,
            onScan = trendRadarActions.onScan,
            onSaveOpportunity = trendRadarActions.onSaveOpportunity,
            modifier = modifier,
        )
        BrandForgeDestination.Studio -> ContentStudioScreen(
            state = contentStudioState,
            onCreatorIdChange = contentStudioActions.onCreatorIdChange,
            onGenerate = contentStudioActions.onGenerate,
            onMediaPromptChange = contentStudioActions.onMediaPromptChange,
            onGenerateMedia = contentStudioActions.onGenerateMedia,
            modifier = modifier,
        )
        BrandForgeDestination.Leads -> LeadInboxScreen(
            state = leadInboxState,
            onCreatorIdChange = leadInboxActions.onCreatorIdChange,
            onSourceTypeChange = leadInboxActions.onSourceTypeChange,
            onPlatformChange = leadInboxActions.onPlatformChange,
            onAuthorHandleChange = leadInboxActions.onAuthorHandleChange,
            onInteractionTextChange = leadInboxActions.onInteractionTextChange,
            onClassify = leadInboxActions.onClassify,
            modifier = modifier,
        )
        BrandForgeDestination.Competitors -> CompetitorScreen(
            state = competitorState,
            onCreatorIdChange = competitorActions.onCreatorIdChange,
            onCompetitorUrlChange = competitorActions.onCompetitorUrlChange,
            onAnalyze = competitorActions.onAnalyze,
            modifier = modifier,
        )
        BrandForgeDestination.PrRisk -> PrRiskAuditScreen(
            state = prRiskAuditState,
            onCreatorIdChange = prRiskAuditActions.onCreatorIdChange,
            onMediaUriChange = prRiskAuditActions.onMediaUriChange,
            onMediaContextChange = prRiskAuditActions.onMediaContextChange,
            onCaptionChange = prRiskAuditActions.onCaptionChange,
            onAudit = prRiskAuditActions.onAudit,
            modifier = modifier,
        )
        BrandForgeDestination.WarRoom -> WarRoomScreen(
            commandState = state,
            state = warRoomState,
            onCreatorIdChange = warRoomActions.onCreatorIdChange,
            onBriefChange = warRoomActions.onBriefChange,
            onRunBattle = warRoomActions.onRunBattle,
            onPrepareWarRoom = { onWorkflow(WorkflowCommand.PrepareWarRoom) },
            modifier = modifier,
        )
        BrandForgeDestination.Twin -> RealTwinChatScreen(
            state = twinChatState,
            onCreatorIdChange = twinChatActions.onCreatorIdChange,
            onDraftMessageChange = twinChatActions.onDraftMessageChange,
            onSend = twinChatActions.onSend,
            modifier = modifier,
        )
    }
}

@Composable
private fun BrandHeader(
    state: BrandForgeState,
    compact: Boolean,
    onLogoTap: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ForgeColor.Black.copy(alpha = 0.86f))
            .padding(horizontal = if (compact) 12.dp else 0.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedBrandLogo(
            modifier = Modifier.clickable(onClick = onLogoTap),
            size = if (compact) 42.dp else 52.dp,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onLogoTap),
        ) {
            Text(
                text = "BRANDFORGE",
                style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displayLarge,
                color = ForgeColor.Yellow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Autonomous AI Social Media Engine",
                style = MaterialTheme.typography.bodyMedium,
                color = ForgeColor.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        StatusPill(label = state.systemStatus.take(24), riskLevel = RiskLevel.Low)
    }
}

@Composable
private fun TerminalNavigation(
    selectedDestination: BrandForgeDestination,
    onDestinationSelected: (BrandForgeDestination) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ForgeColor.Black)
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BrandForgeDestination.entries.forEach { destination ->
            PixelButton(
                label = destination.shortLabel,
                active = selectedDestination == destination,
                onClick = { onDestinationSelected(destination) },
                modifier = Modifier.width(68.dp),
            )
        }
    }
}

@Composable
private fun CommandCenterScreen(
    state: BrandForgeState,
    onWorkflow: (WorkflowCommand) -> Unit,
    onVoiceTranscript: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val scroll = rememberScrollState()
        if (maxWidth >= 720.dp) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .verticalScroll(scroll),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TwinPanel(state)
                    WorkflowPanel(state, onWorkflow, onVoiceTranscript)
                    TrendPanel(state.trends)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MetricsPanel(state.creatorMetrics)
                    ContentQueuePanel(state.contentQueue)
                    LiveLogsPanel(state.logs)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TwinPanel(state)
                WorkflowPanel(state, onWorkflow, onVoiceTranscript)
                AgentPulsePanel(state.agents.take(4))
                MetricsPanel(state.creatorMetrics)
                TrendPanel(state.trends)
                ContentQueuePanel(state.contentQueue)
                LiveLogsPanel(state.logs)
            }
        }
    }
}

@Composable
private fun TwinPanel(state: BrandForgeState) {
    ForgePanel {
        SectionHeader("Digital Twin", trailing = state.twin.maturityLevel)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PixelTwinAvatar()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.twin.creatorName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = ForgeColor.White,
                    maxLines = 1,
                )
                Text(
                    text = state.twin.brandArchetype,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Yellow,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(8.dp))
                SignalBar(progress = state.twin.voiceLockPercent / 100f, color = ForgeColor.Green)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        KeyValueLine("Voice lock", "${state.twin.voiceLockPercent}%", valueColor = ForgeColor.Green)
        KeyValueLine("Memory vectors", state.twin.memoryVectors.toString())
        KeyValueLine("Audience segments", state.twin.activeAudienceSegments.toString())
        KeyValueLine("Source of truth", state.twin.sourceOfTruth, valueColor = ForgeColor.Yellow)
        KeyValueLine("Last learning", state.twin.lastLearningCycle)
    }
}

@Composable
private fun WorkflowPanel(
    state: BrandForgeState,
    onWorkflow: (WorkflowCommand) -> Unit,
    onVoiceTranscript: (String) -> Unit,
) {
    ForgePanel {
        SectionHeader("Agent Workflows", trailing = state.activeWorkflow?.label ?: "STANDBY")
        Spacer(modifier = Modifier.height(10.dp))
        WorkflowCommand.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { command ->
                    PixelButton(
                        label = command.label,
                        active = state.activeWorkflow == command,
                        onClick = { onWorkflow(command) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        VoiceCommandPanel(onVoiceTranscript = onVoiceTranscript)
    }
}

@Composable
private fun VoiceCommandPanel(onVoiceTranscript: (String) -> Unit) {
    val context = LocalContext.current
    var voiceStatus by remember { mutableStateOf("SpeechRecognizer standby") }
    val recognizer = remember {
        AndroidSpeechCommandRecognizer(
            context = context,
            onTranscript = {
                voiceStatus = "VOICE: $it"
                onVoiceTranscript(it)
            },
            onPartialTranscript = {
                voiceStatus = "Hearing: $it"
            },
            onError = { voiceStatus = it },
        )
    }
    DisposableEffect(Unit) {
        onDispose { recognizer.release() }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            voiceStatus = "Listening..."
            recognizer.start()
        } else {
            voiceStatus = "Mic permission denied"
        }
    }

    TerminalDivider()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PixelButton(
            label = "MIC",
            onClick = {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    voiceStatus = "Listening..."
                    recognizer.start()
                } else {
                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier.width(72.dp),
        )
        Text(
            text = voiceStatus,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetricsPanel(metrics: List<CreatorMetric>) {
    ForgePanel {
        SectionHeader("Performance Matrix", trailing = "LIVE")
        Spacer(modifier = Modifier.height(10.dp))
        metrics.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { metric ->
                    MetricCell(metric = metric, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MetricCell(metric: CreatorMetric, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .heightIn(min = 84.dp)
            .border(1.dp, ForgeColor.White.copy(alpha = 0.36f))
            .background(ForgeColor.PanelRaised.copy(alpha = 0.74f))
            .padding(9.dp),
    ) {
        Text(
            text = metric.label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = ForgeColor.Muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = metric.value,
            style = MaterialTheme.typography.headlineMedium,
            color = ForgeColor.White,
            maxLines = 1,
        )
        Text(
            text = metric.delta,
            style = MaterialTheme.typography.bodyMedium,
            color = if (metric.riskLevel == RiskLevel.High) ForgeColor.Red else ForgeColor.Green,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(6.dp))
        SignalBar(progress = metric.progress, height = 5.dp, color = ForgeColor.Yellow)
    }
}

@Composable
private fun AgentPulsePanel(agents: List<AgentState>) {
    ForgePanel {
        SectionHeader("Agent Pulse", trailing = "${agents.size} ACTIVE")
        Spacer(modifier = Modifier.height(10.dp))
        agents.forEach { agent ->
            AgentRow(agent)
            Spacer(modifier = Modifier.height(9.dp))
        }
    }
}

@Composable
private fun AgentRow(agent: AgentState) {
    val modeColor = when (agent.mode) {
        AgentMode.Escalated -> ForgeColor.Red
        AgentMode.HandoffReady -> ForgeColor.Yellow
        AgentMode.Idle -> ForgeColor.Muted
        else -> ForgeColor.Green
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, modeColor.copy(alpha = 0.65f))
            .background(ForgeColor.Black.copy(alpha = 0.58f))
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${agent.type.callSign} / ${agent.type.displayName}",
                style = MaterialTheme.typography.labelMedium,
                color = ForgeColor.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = agent.mode.name.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = modeColor,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = agent.currentTask,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(7.dp))
        SignalBar(agent.progress, color = modeColor)
    }
}

@Composable
private fun TrendPanel(trends: List<TrendOpportunity>) {
    ForgePanel {
        SectionHeader("Trend Opportunities", trailing = "SCORED")
        Spacer(modifier = Modifier.height(10.dp))
        trends.forEach { trend ->
            SignalItem(
                title = trend.title,
                meta = "${trend.platform} / ${trend.window} / ${trend.recommendedFormat}",
                score = trend.score,
                rationale = trend.rationale,
                riskLevel = trend.riskLevel,
            )
            Spacer(modifier = Modifier.height(9.dp))
        }
    }
}

@Composable
private fun ContentQueuePanel(items: List<ContentQueueItem>) {
    ForgePanel {
        SectionHeader("Content Queue", trailing = "${items.size} DRAFTS")
        Spacer(modifier = Modifier.height(10.dp))
        items.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ForgeColor.White.copy(alpha = 0.34f))
                    .padding(8.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(label = item.format)
                    StatusPill(label = "VIR ${item.viralityScore}", riskLevel = RiskLevel.Low)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ForgeColor.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${item.status} / Brand fit ${item.brandFit}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForgeColor.Muted,
                    maxLines = 1,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SignalItem(
    title: String,
    meta: String,
    score: Int,
    rationale: String,
    riskLevel: RiskLevel,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForgeColor.White.copy(alpha = 0.34f))
            .background(ForgeColor.PanelRaised.copy(alpha = 0.5f))
            .padding(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ForgeColor.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatusPill(label = score.toString(), riskLevel = riskLevel)
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = meta,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Yellow,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = rationale,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LiveLogsPanel(logs: List<AgentLog>, modifier: Modifier = Modifier) {
    ForgePanel(modifier = modifier) {
        SectionHeader("Live Agent Console", trailing = "STREAM")
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            logs.forEach { log ->
                Text(
                    text = "> ${log.timestamp} [${log.agent.callSign}] ${log.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (log.riskLevel) {
                        RiskLevel.Low -> ForgeColor.Green
                        RiskLevel.Medium -> ForgeColor.Yellow
                        RiskLevel.High -> ForgeColor.Red
                    },
                )
            }
        }
    }
}

@Composable
private fun AgentsScreen(
    state: BrandForgeState,
    onWorkflow: (WorkflowCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WorkflowPanel(state, onWorkflow, onVoiceTranscript = {})
        AgentPulsePanel(state.agents)
    }
}

@Composable
private fun StudioScreen(
    state: BrandForgeState,
    onWorkflow: (WorkflowCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ForgePanel {
            SectionHeader("Content Studio", trailing = "RAG LOCKED")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reels scripts / carousels / X threads / LinkedIn / Shorts / WhatsApp broadcasts",
                style = MaterialTheme.typography.bodyMedium,
                color = ForgeColor.Muted,
            )
            Spacer(modifier = Modifier.height(10.dp))
            PixelButton(
                label = "Generate memory-aware content",
                onClick = { onWorkflow(WorkflowCommand.GenerateContent) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        ContentQueuePanel(state.contentQueue)
        TrendPanel(state.trends)
    }
}

@Composable
private fun OfficeKitScreen(
    state: BrandForgeState,
    onWorkflow: (WorkflowCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ForgePanel {
            SectionHeader("Office Kit", trailing = state.twin.officeKitMode.name.uppercase())
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Phone remains canonical. Laptop War Room receives content, trends, reports, and competitor analysis handoffs.",
                style = MaterialTheme.typography.bodyMedium,
                color = ForgeColor.Muted,
            )
            Spacer(modifier = Modifier.height(10.dp))
            KeyValueLine("Source", state.twin.sourceOfTruth, valueColor = ForgeColor.Yellow)
            KeyValueLine("Handoff", "content / trends / reports / competitors")
            Spacer(modifier = Modifier.height(10.dp))
            PixelButton(
                label = "Prepare War Room handoff",
                onClick = { onWorkflow(WorkflowCommand.PrepareWarRoom) },
                modifier = Modifier.fillMaxWidth(),
                active = state.activeWorkflow == WorkflowCommand.PrepareWarRoom,
            )
        }
        ForgePanel {
            SectionHeader("Strategy Brief", trailing = "READY")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.strategyBrief.title, style = MaterialTheme.typography.titleMedium, color = ForgeColor.White)
            Spacer(modifier = Modifier.height(8.dp))
            KeyValueLine("Morning", state.strategyBrief.morningBriefing)
            KeyValueLine("Weekly", state.strategyBrief.weeklyMove)
            KeyValueLine("PR watch", state.strategyBrief.prWatch, valueColor = ForgeColor.Yellow)
            KeyValueLine("Next", state.strategyBrief.nextBestAction, valueColor = ForgeColor.Green)
        }
        ForgePanel {
            SectionHeader("Competitor Signals", trailing = "${state.competitors.size} TRACKED")
            Spacer(modifier = Modifier.height(8.dp))
            state.competitors.forEach { competitor ->
                SignalItem(
                    title = competitor.competitor,
                    meta = competitor.signal,
                    score = when (competitor.urgency) {
                        RiskLevel.Low -> 72
                        RiskLevel.Medium -> 84
                        RiskLevel.High -> 96
                    },
                    rationale = competitor.opportunity,
                    riskLevel = competitor.urgency,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

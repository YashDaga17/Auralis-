package com.brandforge.app.presentation.commandcenter

import androidx.lifecycle.ViewModel
import com.brandforge.app.core.model.WorkflowCommand
import com.brandforge.app.data.CommandCenterRepository
import com.brandforge.app.domain.voice.VoiceCommandParser
import com.brandforge.app.presentation.navigation.BrandForgeDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CommandCenterViewModel @Inject constructor(
    private val repository: CommandCenterRepository,
    private val voiceCommandParser: VoiceCommandParser,
) : ViewModel() {
    val uiState = repository.state

    private val _selectedDestination = MutableStateFlow(BrandForgeDestination.Command)
    val selectedDestination: StateFlow<BrandForgeDestination> = _selectedDestination.asStateFlow()

    fun selectDestination(destination: BrandForgeDestination) {
        _selectedDestination.value = destination
    }

    fun runWorkflow(command: WorkflowCommand) {
        repository.dispatch(command)
        _selectedDestination.value = when (command) {
            WorkflowCommand.GenerateMorningBriefing -> BrandForgeDestination.WarRoom
            WorkflowCommand.ScanTrends -> BrandForgeDestination.Trends
            WorkflowCommand.GenerateContent -> BrandForgeDestination.Studio
            WorkflowCommand.AnalyzeCompetitors -> BrandForgeDestination.Competitors
            WorkflowCommand.DetectLeads -> BrandForgeDestination.Leads
            WorkflowCommand.AuditPrRisk -> BrandForgeDestination.PrRisk
            WorkflowCommand.PrepareWarRoom -> BrandForgeDestination.WarRoom
        }
    }

    fun routeVoiceCommand(transcript: String) {
        voiceCommandParser.parse(transcript)?.let { command ->
            repository.dispatch(command, transcript)
            _selectedDestination.value = when (command) {
                WorkflowCommand.GenerateContent -> BrandForgeDestination.Studio
                WorkflowCommand.AnalyzeCompetitors -> BrandForgeDestination.Competitors
                WorkflowCommand.PrepareWarRoom -> BrandForgeDestination.WarRoom
                WorkflowCommand.ScanTrends -> BrandForgeDestination.Trends
                WorkflowCommand.GenerateMorningBriefing -> BrandForgeDestination.WarRoom
                WorkflowCommand.DetectLeads -> BrandForgeDestination.Leads
                WorkflowCommand.AuditPrRisk -> BrandForgeDestination.PrRisk
            }
        }
    }
}

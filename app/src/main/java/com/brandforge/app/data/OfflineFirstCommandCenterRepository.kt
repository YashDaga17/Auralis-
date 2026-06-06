package com.brandforge.app.data

import com.brandforge.app.core.model.BrandForgeState
import com.brandforge.app.core.model.WorkflowCommand
import com.brandforge.app.domain.agent.AgentWorkflowEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class OfflineFirstCommandCenterRepository @Inject constructor(
    private val workflowEngine: AgentWorkflowEngine,
) : CommandCenterRepository {
    private val _state = MutableStateFlow(BrandForgeSeedData.initialState())
    override val state: StateFlow<BrandForgeState> = _state.asStateFlow()

    override fun dispatch(command: WorkflowCommand, transcript: String?) {
        _state.value = workflowEngine.reduce(
            current = _state.value,
            command = command,
            transcript = transcript,
        )
    }
}

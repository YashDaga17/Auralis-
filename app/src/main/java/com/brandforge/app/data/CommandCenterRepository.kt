package com.brandforge.app.data

import com.brandforge.app.core.model.BrandForgeState
import com.brandforge.app.core.model.WorkflowCommand
import kotlinx.coroutines.flow.StateFlow

interface CommandCenterRepository {
    val state: StateFlow<BrandForgeState>

    fun dispatch(command: WorkflowCommand, transcript: String? = null)
}

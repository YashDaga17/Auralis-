package com.brandforge.app.domain.voice

import com.brandforge.app.core.model.WorkflowCommand
import javax.inject.Inject

class VoiceCommandParser @Inject constructor() {
    fun parse(transcript: String): WorkflowCommand? {
        val normalized = transcript.lowercase().trim()
        return when {
            normalized.hasAny("content", "reel", "carousel") || normalized.hasWord("post") ->
                WorkflowCommand.GenerateContent

            normalized.hasAny("competitor", "rival") ->
                WorkflowCommand.AnalyzeCompetitors

            normalized.hasAny("strategy", "brief", "briefing", "morning") ->
                WorkflowCommand.GenerateMorningBriefing

            normalized.hasAny("trend", "viral") ->
                WorkflowCommand.ScanTrends

            normalized.hasAny("lead", "client", "prospect") ->
                WorkflowCommand.DetectLeads

            normalized.hasAny("office", "war room", "laptop", "handoff") ->
                WorkflowCommand.PrepareWarRoom

            normalized.hasAny("risk", "crisis") || normalized.hasWord("pr") ->
                WorkflowCommand.AuditPrRisk

            else -> null
        }
    }

    private fun String.hasAny(vararg terms: String): Boolean = terms.any(::contains)

    private fun String.hasWord(word: String): Boolean =
        Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(this)
}

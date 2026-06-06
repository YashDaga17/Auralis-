package com.brandforge.app.core.debug

import com.brandforge.app.core.database.dao.DebugChecklistDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DebugChecklistManager @Inject constructor(
    private val debugChecklistDao: DebugChecklistDao,
) {
    fun observeChecklist(): Flow<List<DebugChecklistItem>> =
        debugChecklistDao.observeAll().map { rows ->
            rows.map { it.toDomain() }
        }

    suspend fun ensureDefaults() {
        debugChecklistDao.insertDefaults(DefaultChecklist.map { it.toEntity() })
    }

    suspend fun updateStatus(itemId: String, status: DebugChecklistStatus) {
        val current = debugChecklistDao.getById(itemId) ?: return
        debugChecklistDao.upsert(
            current.copy(
                status = status.name,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    private companion object {
        val DefaultChecklist = listOf(
            checklistItem(
                id = "brand-dna-save",
                label = "Brand DNA Save",
                expectedResult = "Creator profile saves and status returns PASS without local database errors.",
                failureConditions = "Save button fails, error banner appears, or Brand DNA cannot be found in debug health checks.",
                criticality = "P0",
            ),
            checklistItem(
                id = "brand-dna-reload",
                label = "Brand DNA Reload",
                expectedResult = "Saved Creator DNA reloads after app navigation and app restart.",
                failureConditions = "Fields reset unexpectedly or active creator ID changes without user action.",
                criticality = "P0",
            ),
            checklistItem(
                id = "memory-save",
                label = "Memory Save",
                expectedResult = "New memory persists locally and Qdrant sync failure does not block local storage.",
                failureConditions = "Memory disappears, write crashes, or local-first behavior is broken.",
                criticality = "P0",
            ),
            checklistItem(
                id = "memory-retrieval",
                label = "Memory Retrieval",
                expectedResult = "A natural query returns relevant creator memories with remote or local fallback.",
                failureConditions = "Empty result despite seeded memory, wrong creator memories, or retrieval crash.",
                criticality = "P0",
            ),
            checklistItem(
                id = "trend-scan",
                label = "Trend Scan",
                expectedResult = "Trend scan completes using configured Firecrawl/YouTube keys or clearly reports API failure.",
                failureConditions = "Indefinite loading, hard crash, or silent empty result.",
                criticality = "P1",
            ),
            checklistItem(
                id = "opportunity-creation",
                label = "Opportunity Creation",
                expectedResult = "Scored opportunities persist with source, brand fit, score, format, and rationale.",
                failureConditions = "Signals appear without opportunities, missing rationale, or scores outside expected range.",
                criticality = "P1",
            ),
            checklistItem(
                id = "content-generation",
                label = "Content Generation",
                expectedResult = "A selected opportunity creates a Reel, Carousel, or X Thread draft tied to creator context.",
                failureConditions = "Generic output, missing why-generated reason, missing source trend, or generation crash.",
                criticality = "P0",
            ),
            checklistItem(
                id = "twin-chat",
                label = "Twin Chat",
                expectedResult = "The Twin answers with Brand DNA, memory, trend, content, and competitor citations.",
                failureConditions = "ChatGPT-like generic answer, missing citations, or conversation not persisted.",
                criticality = "P0",
            ),
            checklistItem(
                id = "lead-classification",
                label = "Lead Classification",
                expectedResult = "Comment or DM-like text is classified with confidence, priority, reason, and reply.",
                failureConditions = "Missing confidence/reply, invalid class, or stored lead not visible after restart.",
                criticality = "P1",
            ),
            checklistItem(
                id = "competitor-analysis",
                label = "Competitor Analysis",
                expectedResult = "Competitor URL stores content and gap insights from Firecrawl/YouTube sources.",
                failureConditions = "No stored competitor, insights missing hook/angle, or dashboard uses placeholders.",
                criticality = "P1",
            ),
            checklistItem(
                id = "voice-commands",
                label = "Voice Commands",
                expectedResult = "SpeechRecognizer permission flow works and routed commands trigger app workflows.",
                failureConditions = "Permission loop, recognizer crash, or transcript does not route to a workflow.",
                criticality = "P1",
            ),
            checklistItem(
                id = "navigation",
                label = "Navigation",
                expectedResult = "Every visible tab opens on phone and larger Office Kit layouts without broken state.",
                failureConditions = "Back stack lock, unreadable screen, lost state, or inaccessible workflow.",
                criticality = "P0",
            ),
            checklistItem(
                id = "persistence-after-restart",
                label = "Persistence After Restart",
                expectedResult = "Brand DNA, memories, opportunities, drafts, leads, competitors, and chats survive app restart.",
                failureConditions = "Data loss after force close or wrong creator data shown.",
                criticality = "P0",
            ),
        )

        fun checklistItem(
            id: String,
            label: String,
            expectedResult: String,
            failureConditions: String,
            criticality: String,
        ): DebugChecklistItem =
            DebugChecklistItem(
                id = id,
                label = label,
                status = DebugChecklistStatus.NotTested,
                expectedResult = expectedResult,
                failureConditions = failureConditions,
                criticality = criticality,
                updatedAt = 0L,
                notes = "",
            )
    }
}

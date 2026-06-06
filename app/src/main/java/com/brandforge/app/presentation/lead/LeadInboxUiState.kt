package com.brandforge.app.presentation.lead

import com.brandforge.app.domain.lead.AudienceInteractionType
import com.brandforge.app.domain.lead.Lead

data class LeadInboxUiState(
    val creatorId: String = "",
    val sourceType: AudienceInteractionType = AudienceInteractionType.Comment,
    val platform: String = "Instagram",
    val authorHandle: String = "",
    val interactionText: String = "",
    val classifying: Boolean = false,
    val leads: List<Lead> = emptyList(),
    val errorMessage: String? = null,
)

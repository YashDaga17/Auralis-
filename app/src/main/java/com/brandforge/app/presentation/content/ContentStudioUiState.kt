package com.brandforge.app.presentation.content

import com.brandforge.app.domain.content.ContentDraft
import com.brandforge.app.domain.content.ContentFormat
import com.brandforge.app.domain.content.ContentMediaArtifact
import com.brandforge.app.domain.content.MediaArtifactType
import com.brandforge.app.domain.trend.TrendOpportunity

data class ContentStudioUiState(
    val creatorId: String = "",
    val opportunities: List<TrendOpportunity> = emptyList(),
    val drafts: List<ContentDraft> = emptyList(),
    val mediaPrompt: String = "",
    val mediaArtifacts: List<ContentMediaArtifact> = emptyList(),
    val generatingTrendId: String? = null,
    val generatingFormat: ContentFormat? = null,
    val generatingMediaType: MediaArtifactType? = null,
    val errorMessage: String? = null,
)

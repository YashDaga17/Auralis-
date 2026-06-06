package com.brandforge.app.data.lead

import com.brandforge.app.core.database.entity.LeadEntity
import com.brandforge.app.domain.lead.AudienceInteractionType
import com.brandforge.app.domain.lead.Lead
import com.brandforge.app.domain.lead.LeadClassification
import com.brandforge.app.domain.lead.LeadDetectionInput
import com.brandforge.app.domain.lead.LeadPriority

fun LeadEntity.toDomain(): Lead =
    Lead(
        id = id,
        creatorId = creatorId,
        sourceType = AudienceInteractionType.valueOf(sourceType),
        platform = platform,
        authorHandle = authorHandle,
        text = text,
        classification = LeadClassification.valueOf(classification),
        confidence = confidence,
        suggestedReply = suggestedReply,
        priority = LeadPriority.valueOf(priority),
        reason = reason,
        receivedAt = receivedAt,
        classifiedAt = classifiedAt,
        rawModelResponse = rawModelResponse,
    )

fun LeadDetectionInput.toEntity(): LeadEntity =
    LeadEntity(
        id = id,
        creatorId = creatorId,
        sourceType = sourceType.name,
        platform = platform,
        authorHandle = authorHandle,
        text = text,
        classification = classification.name,
        confidence = confidence.coerceIn(0f, 1f),
        suggestedReply = suggestedReply,
        priority = priority.name,
        reason = reason,
        receivedAt = receivedAt,
        classifiedAt = classifiedAt,
        rawModelResponse = rawModelResponse,
    )

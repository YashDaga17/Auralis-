package com.brandforge.app.data.competitor

import com.brandforge.app.core.database.entity.CompetitorContentEntity
import com.brandforge.app.core.database.entity.CompetitorEntity
import com.brandforge.app.core.database.entity.CompetitorInsightEntity
import com.brandforge.app.domain.competitor.Competitor
import com.brandforge.app.domain.competitor.CompetitorContent
import com.brandforge.app.domain.competitor.CompetitorContentInput
import com.brandforge.app.domain.competitor.CompetitorInput
import com.brandforge.app.domain.competitor.CompetitorInsight
import com.brandforge.app.domain.competitor.CompetitorInsightInput
import com.brandforge.app.domain.competitor.CompetitorPlatform

fun CompetitorEntity.toDomain(): Competitor =
    Competitor(
        id = id,
        creatorId = creatorId,
        name = name,
        platform = CompetitorPlatform.valueOf(platform),
        url = url,
        lastAnalyzed = lastAnalyzed,
    )

fun CompetitorInput.toEntity(): CompetitorEntity =
    CompetitorEntity(
        id = id,
        creatorId = creatorId,
        name = name,
        platform = platform.name,
        url = url,
        lastAnalyzed = lastAnalyzed,
    )

fun CompetitorContentEntity.toDomain(): CompetitorContent =
    CompetitorContent(
        id = id,
        competitorId = competitorId,
        creatorId = creatorId,
        title = title,
        summary = summary,
        publishedAt = publishedAt,
        engagementEstimate = engagementEstimate,
        sourceUrl = sourceUrl,
        rawPayloadJson = rawPayloadJson,
        observedAt = observedAt,
    )

fun CompetitorContentInput.toEntity(): CompetitorContentEntity =
    CompetitorContentEntity(
        id = id,
        competitorId = competitorId,
        creatorId = creatorId,
        title = title,
        summary = summary,
        publishedAt = publishedAt,
        engagementEstimate = engagementEstimate,
        sourceUrl = sourceUrl,
        rawPayloadJson = rawPayloadJson,
        observedAt = observedAt,
    )

fun CompetitorInsightEntity.toDomain(): CompetitorInsight =
    CompetitorInsight(
        id = id,
        competitorId = competitorId,
        creatorId = creatorId,
        pattern = pattern,
        frequency = frequency,
        gap = gap,
        recommendation = recommendation,
        confidence = confidence,
        reasoning = reasoning,
        recommendedContentFormat = recommendedContentFormat,
        recommendedHook = recommendedHook,
        recommendedAngle = recommendedAngle,
        opportunityScore = opportunityScore,
        createdAt = createdAt,
    )

fun CompetitorInsightInput.toEntity(): CompetitorInsightEntity =
    CompetitorInsightEntity(
        id = id,
        competitorId = competitorId,
        creatorId = creatorId,
        pattern = pattern,
        frequency = frequency,
        gap = gap,
        recommendation = recommendation,
        confidence = confidence.coerceIn(0f, 1f),
        reasoning = reasoning,
        recommendedContentFormat = recommendedContentFormat,
        recommendedHook = recommendedHook,
        recommendedAngle = recommendedAngle,
        opportunityScore = opportunityScore.coerceIn(0f, 1f),
        createdAt = createdAt,
    )

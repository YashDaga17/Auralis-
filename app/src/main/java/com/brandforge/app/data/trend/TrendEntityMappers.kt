package com.brandforge.app.data.trend

import com.brandforge.app.core.database.entity.TrendOpportunityEntity
import com.brandforge.app.core.database.entity.TrendSignalEntity
import com.brandforge.app.domain.trend.TrendOpportunity
import com.brandforge.app.domain.trend.TrendSignal

fun TrendSignalEntity.toDomain(): TrendSignal =
    TrendSignal(
        id = id,
        creatorId = creatorId,
        sourceUrl = sourceUrl,
        sourcePlatform = sourcePlatform,
        title = title,
        summary = summary,
        sourceRank = sourceRank,
        observedAt = observedAt,
        publishedAt = publishedAt,
        rawPayloadJson = rawPayloadJson,
    )

fun TrendSignal.toEntity(): TrendSignalEntity =
    TrendSignalEntity(
        id = id,
        creatorId = creatorId,
        sourceUrl = sourceUrl,
        sourcePlatform = sourcePlatform,
        title = title,
        summary = summary,
        sourceRank = sourceRank,
        observedAt = observedAt,
        publishedAt = publishedAt,
        rawPayloadJson = rawPayloadJson,
    )

fun TrendOpportunityEntity.toDomain(): TrendOpportunity =
    TrendOpportunity(
        id = id,
        creatorId = creatorId,
        signalId = signalId,
        sourceUrl = sourceUrl,
        sourcePlatform = sourcePlatform,
        title = title,
        summary = summary,
        velocityScore = velocityScore,
        freshnessScore = freshnessScore,
        brandFitScore = brandFitScore,
        opportunityScore = opportunityScore,
        recommendedFormat = recommendedFormat,
        rationale = rationale,
        createdAt = createdAt,
    )

fun TrendOpportunity.toEntity(): TrendOpportunityEntity =
    TrendOpportunityEntity(
        id = id,
        creatorId = creatorId,
        signalId = signalId,
        sourceUrl = sourceUrl,
        sourcePlatform = sourcePlatform,
        title = title,
        summary = summary,
        velocityScore = velocityScore,
        freshnessScore = freshnessScore,
        brandFitScore = brandFitScore,
        opportunityScore = opportunityScore,
        recommendedFormat = recommendedFormat,
        rationale = rationale,
        createdAt = createdAt,
    )

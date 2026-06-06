package com.brandforge.app.core.debug

import com.brandforge.app.core.database.entity.DebugChecklistItemEntity
import com.brandforge.app.core.database.entity.DebugErrorEntity

fun DebugErrorEntity.toDomain(): DebugErrorLog =
    DebugErrorLog(
        id = id,
        timestamp = timestamp,
        feature = feature,
        screen = screen,
        message = message,
        stackTrace = stackTrace,
        severity = runCatching { DebugErrorSeverity.valueOf(severity) }.getOrDefault(DebugErrorSeverity.Error),
    )

fun DebugErrorLog.toEntity(): DebugErrorEntity =
    DebugErrorEntity(
        id = id,
        timestamp = timestamp,
        feature = feature,
        screen = screen,
        message = message,
        stackTrace = stackTrace,
        severity = severity.name,
    )

fun DebugChecklistItemEntity.toDomain(): DebugChecklistItem =
    DebugChecklistItem(
        id = id,
        label = label,
        status = runCatching { DebugChecklistStatus.valueOf(status) }.getOrDefault(DebugChecklistStatus.NotTested),
        expectedResult = expectedResult,
        failureConditions = failureConditions,
        criticality = criticality,
        updatedAt = updatedAt,
        notes = notes,
    )

fun DebugChecklistItem.toEntity(): DebugChecklistItemEntity =
    DebugChecklistItemEntity(
        id = id,
        label = label,
        status = status.name,
        expectedResult = expectedResult,
        failureConditions = failureConditions,
        criticality = criticality,
        updatedAt = updatedAt,
        notes = notes,
    )

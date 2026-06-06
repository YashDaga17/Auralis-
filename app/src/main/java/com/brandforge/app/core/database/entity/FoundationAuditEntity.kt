package com.brandforge.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foundation_audit")
data class FoundationAuditEntity(
    @PrimaryKey
    val id: String = LatestId,
    val checkedAtEpochMillis: Long,
    val productionReady: Boolean,
    val missingKeysCsv: String,
    val configuredKeysCsv: String,
) {
    companion object {
        const val LatestId = "latest"
    }
}

package com.brandforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brand_dna")
data class BrandDnaEntity(
    @PrimaryKey
    @ColumnInfo(name = "creator_id")
    val creatorId: String,
    @ColumnInfo(name = "creator_name")
    val creatorName: String,
    val archetype: String,
    @ColumnInfo(name = "voice_rules_json")
    val voiceRulesJson: String,
    @ColumnInfo(name = "banned_claims_json")
    val bannedClaimsJson: String,
    @ColumnInfo(name = "business_goals_json")
    val businessGoalsJson: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

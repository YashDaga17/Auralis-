package com.brandforge.app.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object BrandForgeMigrations {
    val Migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS brand_dna(
                    creator_id TEXT NOT NULL PRIMARY KEY,
                    creator_name TEXT NOT NULL,
                    archetype TEXT NOT NULL,
                    voice_rules_json TEXT NOT NULL,
                    banned_claims_json TEXT NOT NULL,
                    business_goals_json TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS memory_shard(
                    id TEXT NOT NULL PRIMARY KEY,
                    creator_id TEXT NOT NULL,
                    type TEXT NOT NULL,
                    title TEXT NOT NULL,
                    summary TEXT NOT NULL,
                    source_uri TEXT,
                    embedding_id TEXT,
                    retrieval_weight REAL NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS content_sample(
                    id TEXT NOT NULL PRIMARY KEY,
                    creator_id TEXT NOT NULL,
                    platform TEXT NOT NULL,
                    body TEXT NOT NULL,
                    performance_json TEXT NOT NULL,
                    style_features_json TEXT NOT NULL,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_memory_creator_type ON memory_shard(creator_id, type)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_content_creator_platform ON content_sample(creator_id, platform)")
        }
    }

    val Migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS trend_signal(
                    id TEXT NOT NULL PRIMARY KEY,
                    creator_id TEXT NOT NULL,
                    source_url TEXT NOT NULL,
                    source_platform TEXT NOT NULL,
                    title TEXT NOT NULL,
                    summary TEXT NOT NULL,
                    source_rank INTEGER NOT NULL,
                    observed_at INTEGER NOT NULL,
                    published_at INTEGER,
                    raw_payload_json TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS trend_opportunity(
                    id TEXT NOT NULL PRIMARY KEY,
                    creator_id TEXT NOT NULL,
                    signal_id TEXT NOT NULL,
                    source_url TEXT NOT NULL,
                    source_platform TEXT NOT NULL,
                    title TEXT NOT NULL,
                    summary TEXT NOT NULL,
                    velocity_score REAL NOT NULL,
                    freshness_score REAL NOT NULL,
                    brand_fit_score REAL NOT NULL,
                    opportunity_score REAL NOT NULL,
                    recommended_format TEXT NOT NULL,
                    rationale TEXT NOT NULL,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_trend_signal_creator_platform ON trend_signal(creator_id, source_platform)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_trend_opportunity_creator_score ON trend_opportunity(creator_id, opportunity_score)")
        }
    }

    val Migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS content_draft(
                    id TEXT NOT NULL PRIMARY KEY,
                    creator_id TEXT NOT NULL,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    format TEXT NOT NULL,
                    generated_at INTEGER NOT NULL,
                    source_trend_id TEXT NOT NULL,
                    opportunity_score REAL NOT NULL,
                    memory_ids_used TEXT NOT NULL,
                    why_generated TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_content_draft_creator_generated ON content_draft(creator_id, generated_at)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_content_draft_source_trend ON content_draft(source_trend_id)")
        }
    }

    val Migration4To5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS twin_chat_message(
                    id TEXT NOT NULL PRIMARY KEY,
                    creator_id TEXT NOT NULL,
                    role TEXT NOT NULL,
                    message TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    memory_ids TEXT NOT NULL,
                    trend_ids TEXT NOT NULL,
                    opportunity_ids TEXT NOT NULL,
                    content_draft_ids TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_twin_chat_creator_created ON twin_chat_message(creator_id, created_at)")
        }
    }

    val Migration5To6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS lead_detection(
                    id TEXT NOT NULL PRIMARY KEY,
                    creator_id TEXT NOT NULL,
                    source_type TEXT NOT NULL,
                    platform TEXT NOT NULL,
                    author_handle TEXT NOT NULL,
                    text TEXT NOT NULL,
                    classification TEXT NOT NULL,
                    confidence REAL NOT NULL,
                    suggested_reply TEXT NOT NULL,
                    priority TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    received_at INTEGER NOT NULL,
                    classified_at INTEGER NOT NULL,
                    raw_model_response TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_lead_creator_priority ON lead_detection(creator_id, priority, classified_at)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_lead_creator_classification ON lead_detection(creator_id, classification)")
        }
    }

    val Migration6To7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS competitor(
                    id TEXT NOT NULL PRIMARY KEY,
                    creator_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    platform TEXT NOT NULL,
                    url TEXT NOT NULL,
                    last_analyzed INTEGER
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS competitor_content(
                    id TEXT NOT NULL PRIMARY KEY,
                    competitor_id TEXT NOT NULL,
                    creator_id TEXT NOT NULL,
                    title TEXT NOT NULL,
                    summary TEXT NOT NULL,
                    published_at INTEGER,
                    engagement_estimate TEXT NOT NULL,
                    source_url TEXT NOT NULL,
                    raw_payload_json TEXT NOT NULL,
                    observed_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS competitor_insight(
                    id TEXT NOT NULL PRIMARY KEY,
                    competitor_id TEXT NOT NULL,
                    creator_id TEXT NOT NULL,
                    pattern TEXT NOT NULL,
                    frequency TEXT NOT NULL,
                    gap TEXT NOT NULL,
                    recommendation TEXT NOT NULL,
                    confidence REAL NOT NULL,
                    reasoning TEXT NOT NULL,
                    recommended_content_format TEXT NOT NULL,
                    recommended_hook TEXT NOT NULL,
                    recommended_angle TEXT NOT NULL,
                    opportunity_score REAL NOT NULL,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_competitor_creator_url ON competitor(creator_id, url)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_competitor_creator_analyzed ON competitor(creator_id, last_analyzed)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_competitor_content_competitor_published ON competitor_content(competitor_id, published_at)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_competitor_content_creator_published ON competitor_content(creator_id, published_at)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_competitor_insight_creator_confidence ON competitor_insight(creator_id, confidence, created_at)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_competitor_insight_competitor_created ON competitor_insight(competitor_id, created_at)")
        }
    }

    val Migration7To8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS debug_error_log(
                    id TEXT NOT NULL PRIMARY KEY,
                    timestamp INTEGER NOT NULL,
                    feature TEXT NOT NULL,
                    screen TEXT NOT NULL,
                    message TEXT NOT NULL,
                    stack_trace TEXT NOT NULL,
                    severity TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS debug_checklist_item(
                    id TEXT NOT NULL PRIMARY KEY,
                    label TEXT NOT NULL,
                    status TEXT NOT NULL,
                    expected_result TEXT NOT NULL,
                    failure_conditions TEXT NOT NULL,
                    criticality TEXT NOT NULL,
                    updated_at INTEGER NOT NULL,
                    notes TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_debug_error_timestamp ON debug_error_log(timestamp)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_debug_error_feature_timestamp ON debug_error_log(feature, timestamp)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_debug_checklist_status_updated ON debug_checklist_item(status, updated_at)")
        }
    }

    val Migration8To9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS content_media_artifact(
                    id TEXT NOT NULL PRIMARY KEY,
                    creator_id TEXT NOT NULL,
                    type TEXT NOT NULL,
                    prompt TEXT NOT NULL,
                    local_uri TEXT,
                    remote_uri TEXT,
                    mime_type TEXT NOT NULL,
                    model TEXT NOT NULL,
                    status TEXT NOT NULL,
                    error_message TEXT,
                    source_draft_id TEXT,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_content_media_creator_created ON content_media_artifact(creator_id, created_at)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_content_media_source_draft ON content_media_artifact(source_draft_id)")
        }
    }
}

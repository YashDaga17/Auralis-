package com.brandforge.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.brandforge.app.core.database.dao.BrandDnaDao
import com.brandforge.app.core.database.dao.CompetitorContentDao
import com.brandforge.app.core.database.dao.CompetitorDao
import com.brandforge.app.core.database.dao.CompetitorInsightDao
import com.brandforge.app.core.database.dao.ContentSampleDao
import com.brandforge.app.core.database.dao.ContentMediaArtifactDao
import com.brandforge.app.core.database.dao.DebugChecklistDao
import com.brandforge.app.core.database.dao.DebugErrorDao
import com.brandforge.app.core.database.dao.FoundationAuditDao
import com.brandforge.app.core.database.dao.GeneratedContentDao
import com.brandforge.app.core.database.dao.LeadDao
import com.brandforge.app.core.database.dao.MemoryShardDao
import com.brandforge.app.core.database.dao.TrendOpportunityDao
import com.brandforge.app.core.database.dao.TrendSignalDao
import com.brandforge.app.core.database.dao.TwinChatMessageDao
import com.brandforge.app.core.database.entity.BrandDnaEntity
import com.brandforge.app.core.database.entity.CompetitorContentEntity
import com.brandforge.app.core.database.entity.CompetitorEntity
import com.brandforge.app.core.database.entity.CompetitorInsightEntity
import com.brandforge.app.core.database.entity.ContentDraftEntity
import com.brandforge.app.core.database.entity.ContentMediaArtifactEntity
import com.brandforge.app.core.database.entity.ContentSampleEntity
import com.brandforge.app.core.database.entity.DebugChecklistItemEntity
import com.brandforge.app.core.database.entity.DebugErrorEntity
import com.brandforge.app.core.database.entity.FoundationAuditEntity
import com.brandforge.app.core.database.entity.LeadEntity
import com.brandforge.app.core.database.entity.MemoryShardEntity
import com.brandforge.app.core.database.entity.TrendOpportunityEntity
import com.brandforge.app.core.database.entity.TrendSignalEntity
import com.brandforge.app.core.database.entity.TwinChatMessageEntity

@Database(
    entities = [
        FoundationAuditEntity::class,
        BrandDnaEntity::class,
        MemoryShardEntity::class,
        ContentSampleEntity::class,
        TrendSignalEntity::class,
        TrendOpportunityEntity::class,
        ContentDraftEntity::class,
        TwinChatMessageEntity::class,
        LeadEntity::class,
        CompetitorEntity::class,
        CompetitorContentEntity::class,
        CompetitorInsightEntity::class,
        DebugErrorEntity::class,
        DebugChecklistItemEntity::class,
        ContentMediaArtifactEntity::class,
    ],
    version = 9,
    exportSchema = true,
)
abstract class BrandForgeDatabase : RoomDatabase() {
    abstract fun competitorDao(): CompetitorDao
    abstract fun competitorContentDao(): CompetitorContentDao
    abstract fun competitorInsightDao(): CompetitorInsightDao
    abstract fun foundationAuditDao(): FoundationAuditDao
    abstract fun brandDnaDao(): BrandDnaDao
    abstract fun memoryShardDao(): MemoryShardDao
    abstract fun contentSampleDao(): ContentSampleDao
    abstract fun trendSignalDao(): TrendSignalDao
    abstract fun trendOpportunityDao(): TrendOpportunityDao
    abstract fun generatedContentDao(): GeneratedContentDao
    abstract fun contentMediaArtifactDao(): ContentMediaArtifactDao
    abstract fun twinChatMessageDao(): TwinChatMessageDao
    abstract fun leadDao(): LeadDao
    abstract fun debugErrorDao(): DebugErrorDao
    abstract fun debugChecklistDao(): DebugChecklistDao
}

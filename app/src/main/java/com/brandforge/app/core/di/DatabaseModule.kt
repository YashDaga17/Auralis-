package com.brandforge.app.core.di

import android.content.Context
import androidx.room.Room
import com.brandforge.app.core.database.BrandForgeDatabase
import com.brandforge.app.core.database.BrandForgeMigrations
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideBrandForgeDatabase(
        @ApplicationContext context: Context,
    ): BrandForgeDatabase =
        Room.databaseBuilder(
            context,
            BrandForgeDatabase::class.java,
            "brandforge.db",
        )
            .addMigrations(
                BrandForgeMigrations.Migration1To2,
                BrandForgeMigrations.Migration2To3,
                BrandForgeMigrations.Migration3To4,
                BrandForgeMigrations.Migration4To5,
                BrandForgeMigrations.Migration5To6,
                BrandForgeMigrations.Migration6To7,
                BrandForgeMigrations.Migration7To8,
                BrandForgeMigrations.Migration8To9,
            )
            .build()

    @Provides
    fun provideCompetitorDao(database: BrandForgeDatabase): CompetitorDao =
        database.competitorDao()

    @Provides
    fun provideCompetitorContentDao(database: BrandForgeDatabase): CompetitorContentDao =
        database.competitorContentDao()

    @Provides
    fun provideCompetitorInsightDao(database: BrandForgeDatabase): CompetitorInsightDao =
        database.competitorInsightDao()

    @Provides
    fun provideFoundationAuditDao(database: BrandForgeDatabase): FoundationAuditDao =
        database.foundationAuditDao()

    @Provides
    fun provideBrandDnaDao(database: BrandForgeDatabase): BrandDnaDao =
        database.brandDnaDao()

    @Provides
    fun provideMemoryShardDao(database: BrandForgeDatabase): MemoryShardDao =
        database.memoryShardDao()

    @Provides
    fun provideContentSampleDao(database: BrandForgeDatabase): ContentSampleDao =
        database.contentSampleDao()

    @Provides
    fun provideTrendSignalDao(database: BrandForgeDatabase): TrendSignalDao =
        database.trendSignalDao()

    @Provides
    fun provideTrendOpportunityDao(database: BrandForgeDatabase): TrendOpportunityDao =
        database.trendOpportunityDao()

    @Provides
    fun provideGeneratedContentDao(database: BrandForgeDatabase): GeneratedContentDao =
        database.generatedContentDao()

    @Provides
    fun provideContentMediaArtifactDao(database: BrandForgeDatabase): ContentMediaArtifactDao =
        database.contentMediaArtifactDao()

    @Provides
    fun provideTwinChatMessageDao(database: BrandForgeDatabase): TwinChatMessageDao =
        database.twinChatMessageDao()

    @Provides
    fun provideLeadDao(database: BrandForgeDatabase): LeadDao =
        database.leadDao()

    @Provides
    fun provideDebugErrorDao(database: BrandForgeDatabase): DebugErrorDao =
        database.debugErrorDao()

    @Provides
    fun provideDebugChecklistDao(database: BrandForgeDatabase): DebugChecklistDao =
        database.debugChecklistDao()
}

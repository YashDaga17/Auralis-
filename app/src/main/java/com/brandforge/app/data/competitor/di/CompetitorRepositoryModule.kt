package com.brandforge.app.data.competitor.di

import com.brandforge.app.data.competitor.CompetitorRepositoryImpl
import com.brandforge.app.domain.competitor.CompetitorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CompetitorRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCompetitorRepository(
        implementation: CompetitorRepositoryImpl,
    ): CompetitorRepository
}

package com.brandforge.app.data.trend.di

import com.brandforge.app.data.trend.TrendRepositoryImpl
import com.brandforge.app.domain.trend.TrendRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrendRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTrendRepository(
        implementation: TrendRepositoryImpl,
    ): TrendRepository
}

package com.brandforge.app.core.di

import com.brandforge.app.core.config.BuildConfigEnvironmentManager
import com.brandforge.app.core.config.EnvironmentManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EnvironmentModule {
    @Binds
    @Singleton
    abstract fun bindEnvironmentManager(
        implementation: BuildConfigEnvironmentManager,
    ): EnvironmentManager
}

package com.brandforge.app.data.di

import com.brandforge.app.data.CommandCenterRepository
import com.brandforge.app.data.OfflineFirstCommandCenterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommandCenterRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCommandCenterRepository(
        implementation: OfflineFirstCommandCenterRepository,
    ): CommandCenterRepository
}

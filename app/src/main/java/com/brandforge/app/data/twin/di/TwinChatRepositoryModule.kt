package com.brandforge.app.data.twin.di

import com.brandforge.app.data.twin.TwinChatRepositoryImpl
import com.brandforge.app.domain.twin.TwinChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TwinChatRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTwinChatRepository(
        implementation: TwinChatRepositoryImpl,
    ): TwinChatRepository
}

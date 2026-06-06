package com.brandforge.app.data.memory.di

import com.brandforge.app.data.memory.CreatorMemoryRepositoryImpl
import com.brandforge.app.domain.memory.CreatorMemoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MemoryRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCreatorMemoryRepository(
        implementation: CreatorMemoryRepositoryImpl,
    ): CreatorMemoryRepository
}

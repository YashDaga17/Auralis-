package com.brandforge.app.data.content.di

import com.brandforge.app.data.content.ContentRepositoryImpl
import com.brandforge.app.data.content.MediaArtifactRepositoryImpl
import com.brandforge.app.domain.content.ContentRepository
import com.brandforge.app.domain.content.MediaArtifactRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContentRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindContentRepository(
        implementation: ContentRepositoryImpl,
    ): ContentRepository

    @Binds
    @Singleton
    abstract fun bindMediaArtifactRepository(
        implementation: MediaArtifactRepositoryImpl,
    ): MediaArtifactRepository
}

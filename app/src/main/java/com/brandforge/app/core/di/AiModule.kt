package com.brandforge.app.core.di

import com.brandforge.app.core.ai.EmbeddingClient
import com.brandforge.app.core.ai.gemini.GeminiEmbeddingClient
import com.brandforge.app.core.ai.openrouter.OpenRouterClient
import com.brandforge.app.core.ai.openrouter.RetrofitOpenRouterClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds
    @Singleton
    abstract fun bindOpenRouterClient(
        implementation: RetrofitOpenRouterClient,
    ): OpenRouterClient

    @Binds
    @Singleton
    abstract fun bindEmbeddingClient(
        implementation: GeminiEmbeddingClient,
    ): EmbeddingClient
}

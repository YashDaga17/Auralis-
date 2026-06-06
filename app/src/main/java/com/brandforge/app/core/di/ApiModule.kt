package com.brandforge.app.core.di

import com.brandforge.app.core.ai.openrouter.OpenRouterApi
import com.brandforge.app.core.ai.gemini.GeminiGenerationApi
import com.brandforge.app.core.ai.gemini.GeminiEmbeddingApi
import com.brandforge.app.core.ai.gemini.GeminiVideoApi
import com.brandforge.app.core.config.ApiEndpoint
import com.brandforge.app.core.config.EnvironmentManager
import com.brandforge.app.core.network.FirecrawlOkHttp
import com.brandforge.app.core.network.FirecrawlRetrofit
import com.brandforge.app.core.network.GeminiOkHttp
import com.brandforge.app.core.network.GeminiRetrofit
import com.brandforge.app.core.network.OpenRouterOkHttp
import com.brandforge.app.core.network.OpenRouterRetrofit
import com.brandforge.app.core.network.QdrantOkHttp
import com.brandforge.app.core.network.QdrantRetrofit
import com.brandforge.app.core.network.YouTubeOkHttp
import com.brandforge.app.core.network.YouTubeRetrofit
import com.brandforge.app.data.memory.qdrant.QdrantApi
import com.brandforge.app.data.trend.firecrawl.FirecrawlTrendApi
import com.brandforge.app.data.trend.youtube.YouTubeTrendApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    @OpenRouterRetrofit
    fun provideOpenRouterRetrofit(
        environmentManager: EnvironmentManager,
        gson: Gson,
        @OpenRouterOkHttp okHttpClient: OkHttpClient,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(environmentManager.endpointBaseUrl(ApiEndpoint.OpenRouter))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideOpenRouterApi(@OpenRouterRetrofit retrofit: Retrofit): OpenRouterApi =
        retrofit.create(OpenRouterApi::class.java)

    @Provides
    @Singleton
    @GeminiRetrofit
    fun provideGeminiRetrofit(
        environmentManager: EnvironmentManager,
        gson: Gson,
        @GeminiOkHttp okHttpClient: OkHttpClient,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(environmentManager.endpointBaseUrl(ApiEndpoint.Gemini))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideGeminiEmbeddingApi(@GeminiRetrofit retrofit: Retrofit): GeminiEmbeddingApi =
        retrofit.create(GeminiEmbeddingApi::class.java)

    @Provides
    @Singleton
    fun provideGeminiGenerationApi(@GeminiRetrofit retrofit: Retrofit): GeminiGenerationApi =
        retrofit.create(GeminiGenerationApi::class.java)

    @Provides
    @Singleton
    fun provideGeminiVideoApi(@GeminiRetrofit retrofit: Retrofit): GeminiVideoApi =
        retrofit.create(GeminiVideoApi::class.java)

    @Provides
    @Singleton
    @FirecrawlRetrofit
    fun provideFirecrawlRetrofit(
        environmentManager: EnvironmentManager,
        gson: Gson,
        @FirecrawlOkHttp okHttpClient: OkHttpClient,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(environmentManager.endpointBaseUrl(ApiEndpoint.Firecrawl))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideFirecrawlTrendApi(@FirecrawlRetrofit retrofit: Retrofit): FirecrawlTrendApi =
        retrofit.create(FirecrawlTrendApi::class.java)

    @Provides
    @Singleton
    @QdrantRetrofit
    fun provideQdrantRetrofit(
        environmentManager: EnvironmentManager,
        gson: Gson,
        @QdrantOkHttp okHttpClient: OkHttpClient,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(environmentManager.endpointBaseUrl(ApiEndpoint.Qdrant))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideQdrantApi(@QdrantRetrofit retrofit: Retrofit): QdrantApi =
        retrofit.create(QdrantApi::class.java)

    @Provides
    @Singleton
    @YouTubeRetrofit
    fun provideYouTubeRetrofit(
        environmentManager: EnvironmentManager,
        gson: Gson,
        @YouTubeOkHttp okHttpClient: OkHttpClient,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(environmentManager.endpointBaseUrl(ApiEndpoint.YouTube))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideYouTubeTrendApi(@YouTubeRetrofit retrofit: Retrofit): YouTubeTrendApi =
        retrofit.create(YouTubeTrendApi::class.java)
}

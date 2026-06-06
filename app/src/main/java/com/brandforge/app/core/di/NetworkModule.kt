package com.brandforge.app.core.di

import com.brandforge.app.BuildConfig
import com.brandforge.app.core.config.EnvironmentKey
import com.brandforge.app.core.network.ApifyOkHttp
import com.brandforge.app.core.network.AuthInterceptorFactory
import com.brandforge.app.core.network.FirecrawlOkHttp
import com.brandforge.app.core.network.GeminiOkHttp
import com.brandforge.app.core.network.OpenRouterOkHttp
import com.brandforge.app.core.network.QdrantOkHttp
import com.brandforge.app.core.network.YouTubeOkHttp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideGson(): Gson =
        GsonBuilder().disableHtmlEscaping().create()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideBaseOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    @OpenRouterOkHttp
    fun provideOpenRouterOkHttpClient(
        baseClient: OkHttpClient,
        authInterceptorFactory: AuthInterceptorFactory,
    ): OkHttpClient =
        baseClient.newBuilder()
            .addInterceptor(authInterceptorFactory.bearerHeader(EnvironmentKey.OpenRouterApiKey))
            .addInterceptor(authInterceptorFactory.openRouterMetadata())
            .build()

    @Provides
    @Singleton
    @GeminiOkHttp
    fun provideGeminiOkHttpClient(
        baseClient: OkHttpClient,
        authInterceptorFactory: AuthInterceptorFactory,
    ): OkHttpClient =
        baseClient.newBuilder()
            .addInterceptor(authInterceptorFactory.apiKeyQuery(EnvironmentKey.GeminiApiKey))
            .build()

    @Provides
    @Singleton
    @FirecrawlOkHttp
    fun provideFirecrawlOkHttpClient(
        baseClient: OkHttpClient,
        authInterceptorFactory: AuthInterceptorFactory,
    ): OkHttpClient =
        baseClient.newBuilder()
            .addInterceptor(authInterceptorFactory.bearerHeader(EnvironmentKey.FirecrawlApiKey))
            .build()

    @Provides
    @Singleton
    @ApifyOkHttp
    fun provideApifyOkHttpClient(
        baseClient: OkHttpClient,
        authInterceptorFactory: AuthInterceptorFactory,
    ): OkHttpClient =
        baseClient.newBuilder()
            .addInterceptor(authInterceptorFactory.bearerHeader(EnvironmentKey.ApifyApiToken))
            .build()

    @Provides
    @Singleton
    @YouTubeOkHttp
    fun provideYouTubeOkHttpClient(
        baseClient: OkHttpClient,
        authInterceptorFactory: AuthInterceptorFactory,
    ): OkHttpClient =
        baseClient.newBuilder()
            .addInterceptor(authInterceptorFactory.apiKeyQuery(EnvironmentKey.YouTubeApiKey))
            .build()

    @Provides
    @Singleton
    @QdrantOkHttp
    fun provideQdrantOkHttpClient(
        baseClient: OkHttpClient,
        authInterceptorFactory: AuthInterceptorFactory,
    ): OkHttpClient =
        baseClient.newBuilder()
            .addInterceptor(authInterceptorFactory.qdrantApiKey())
            .build()
}

package com.brandforge.app.core.network

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenRouterOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenRouterRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FirecrawlOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FirecrawlRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApifyOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QdrantOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QdrantRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class YouTubeOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class YouTubeRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

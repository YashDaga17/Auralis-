package com.brandforge.app.core.network

import com.brandforge.app.core.config.EnvironmentKey
import com.brandforge.app.core.config.EnvironmentManager
import com.brandforge.app.core.config.SecretManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptorFactory @Inject constructor(
    private val environmentManager: EnvironmentManager,
    private val secretManager: SecretManager,
) {
    fun bearerHeader(key: EnvironmentKey, headerName: String = "Authorization"): Interceptor =
        Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            val secret = environmentManager.secret(key)
            if (secretManager.isConfigured(secret)) {
                requestBuilder.header(headerName, secretManager.bearer(secret))
            }
            chain.proceed(requestBuilder.build())
        }

    fun apiKeyQuery(key: EnvironmentKey, queryName: String = "key"): Interceptor =
        Interceptor { chain ->
            val secret = environmentManager.secret(key)
            if (!secretManager.isConfigured(secret)) {
                return@Interceptor chain.proceed(chain.request())
            }
            val url = chain.request().url.newBuilder()
                .addQueryParameter(queryName, secret)
                .build()
            chain.proceed(chain.request().newBuilder().url(url).build())
        }

    fun qdrantApiKey(): Interceptor =
        Interceptor { chain ->
            val secret = environmentManager.secret(EnvironmentKey.QdrantApiKey)
            val requestBuilder = chain.request().newBuilder()
            if (secretManager.isConfigured(secret)) {
                requestBuilder.header("api-key", secret)
            }
            chain.proceed(requestBuilder.build())
        }

    fun openRouterMetadata(): Interceptor =
        Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("HTTP-Referer", "https://brandforge.local")
                .header("X-Title", "BrandForge")
                .build()
            chain.proceed(request)
        }
}

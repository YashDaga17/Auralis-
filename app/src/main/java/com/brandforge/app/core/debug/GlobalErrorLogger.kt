package com.brandforge.app.core.debug

import android.util.Log
import com.brandforge.app.core.database.dao.DebugErrorDao
import com.brandforge.app.core.network.ApplicationScope
import com.brandforge.app.core.network.IoDispatcher
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class GlobalErrorLogger @Inject constructor(
    private val debugErrorDao: DebugErrorDao,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun log(
        feature: String,
        screen: String,
        throwable: Throwable,
        severity: DebugErrorSeverity = DebugErrorSeverity.Error,
    ) {
        applicationScope.launch(ioDispatcher) {
            write(feature, screen, throwable, severity)
        }
    }

    suspend fun logBlocking(
        feature: String,
        screen: String,
        throwable: Throwable,
        severity: DebugErrorSeverity = DebugErrorSeverity.Error,
    ) {
        withContext(ioDispatcher) {
            write(feature, screen, throwable, severity)
        }
    }

    fun observeLatest(limit: Int = 80): Flow<List<DebugErrorLog>> =
        debugErrorDao.observeLatest(limit).map { rows ->
            rows.map { it.toDomain() }
        }

    suspend fun latest(limit: Int = 80): List<DebugErrorLog> =
        withContext(ioDispatcher) {
            debugErrorDao.latest(limit).map { it.toDomain() }
        }

    suspend fun clear() {
        withContext(ioDispatcher) {
            debugErrorDao.clear()
        }
    }

    private suspend fun write(
        feature: String,
        screen: String,
        throwable: Throwable,
        severity: DebugErrorSeverity,
    ) {
        val message = throwable.message ?: throwable::class.java.simpleName
        debugErrorDao.insert(
            DebugErrorLog(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                feature = feature.take(MaxLabelLength),
                screen = screen.take(MaxLabelLength),
                message = message.take(MaxMessageLength),
                stackTrace = Log.getStackTraceString(throwable).take(MaxStackTraceLength),
                severity = severity,
            ).toEntity(),
        )
    }

    private companion object {
        const val MaxLabelLength = 80
        const val MaxMessageLength = 1_000
        const val MaxStackTraceLength = 12_000
    }
}

package com.brandforge.app.core.startup

import android.util.Log
import com.brandforge.app.core.config.EnvironmentManager
import com.brandforge.app.core.database.dao.FoundationAuditDao
import com.brandforge.app.core.database.entity.FoundationAuditEntity
import com.brandforge.app.core.network.ApplicationScope
import com.brandforge.app.core.network.IoDispatcher
import com.brandforge.app.core.datastore.CreatorPreferencesStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartupCheckRecorder @Inject constructor(
    private val environmentManager: EnvironmentManager,
    private val foundationAuditDao: FoundationAuditDao,
    private val preferencesStore: CreatorPreferencesStore,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun recordStartupCheck() {
        val validation = environmentManager.validate()
        validation.warningMessage()?.let { warning ->
            Log.w(Tag, warning)
        }

        val now = System.currentTimeMillis()
        applicationScope.launch(ioDispatcher) {
            foundationAuditDao.upsert(
                FoundationAuditEntity(
                    checkedAtEpochMillis = now,
                    productionReady = validation.isProductionReady,
                    missingKeysCsv = validation.missingRequiredKeys.joinToString(",") { it.buildConfigName },
                    configuredKeysCsv = validation.configuredKeys.joinToString(",") { it.buildConfigName },
                ),
            )
            preferencesStore.markStartupValidation(now)
        }
    }

    private companion object {
        const val Tag = "BrandForgeStartup"
    }
}

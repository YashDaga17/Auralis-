package com.brandforge.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreatorPreferencesStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val selectedCreatorId: Flow<String?> =
        dataStore.data.map { preferences -> preferences[SelectedCreatorId] }

    val lastStartupValidationEpochMillis: Flow<Long> =
        dataStore.data.map { preferences -> preferences[LastStartupValidationEpochMillis] ?: 0L }

    val brandDnaOnboardingCompleted: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[BrandDnaOnboardingCompleted] ?: false }

    suspend fun setSelectedCreatorId(creatorId: String) {
        dataStore.edit { preferences ->
            preferences[SelectedCreatorId] = creatorId
        }
    }

    suspend fun markStartupValidation(timestampEpochMillis: Long) {
        dataStore.edit { preferences ->
            preferences[LastStartupValidationEpochMillis] = timestampEpochMillis
        }
    }

    suspend fun markBrandDnaOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[BrandDnaOnboardingCompleted] = completed
        }
    }

    private companion object {
        val SelectedCreatorId = stringPreferencesKey("selected_creator_id")
        val LastStartupValidationEpochMillis = longPreferencesKey("last_startup_validation_epoch_millis")
        val BrandDnaOnboardingCompleted = booleanPreferencesKey("brand_dna_onboarding_completed")
    }
}

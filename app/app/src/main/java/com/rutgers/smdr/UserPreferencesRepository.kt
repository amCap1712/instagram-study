package com.rutgers.smdr

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException

data class UserPreferences(
    val surveyComplete: Boolean,
    val surveyUserId: Int?,
    val usageCollectionTime: Long?
)

private const val USER_PREFERENCES_NAME = "user_preferences"

val Context.preferencesDataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private val TAG: String = "UserPreferencesRepo"

    private object PreferencesKeys {
        val SURVEY_COMPLETED = booleanPreferencesKey("survey_completed")
        val SURVEY_USER_ID = intPreferencesKey("survey_user_id")
        val USAGE_COLLECTION_TIME = longPreferencesKey("usage_collection_time")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Timber.tag(TAG).e(exception, "Error reading preferences.")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapUserPreferences(preferences)
        }

    suspend fun updateShowCompleted(surveyCompleted: Boolean, surveyUserId: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SURVEY_COMPLETED] = surveyCompleted
            preferences[PreferencesKeys.SURVEY_USER_ID] = surveyUserId
        }
    }

    suspend fun updateUsageCollectionTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USAGE_COLLECTION_TIME] = time
        }
    }

    suspend fun fetchInitialPreferences() =
        mapUserPreferences(dataStore.data.first().toPreferences())


    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        val showCompleted = preferences[PreferencesKeys.SURVEY_COMPLETED] ?: false
        return UserPreferences(
            showCompleted,
            preferences[PreferencesKeys.SURVEY_USER_ID],
            preferences[PreferencesKeys.USAGE_COLLECTION_TIME]
        )
    }
}

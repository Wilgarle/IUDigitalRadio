package com.example.iudigitalradio.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.iudigitalradio.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    private val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    private val LISTENED_SECONDS = longPreferencesKey("listened_seconds")
    private val LISTENED_COUNTRIES = stringSetPreferencesKey("listened_countries")

    override val isVibrationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    override val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_ENABLED] ?: true
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_ENABLED] = enabled
        }
    }

    override val listenedSeconds: Flow<Long> = dataStore.data.map { preferences ->
        preferences[LISTENED_SECONDS] ?: 0L
    }

    override suspend fun addListenedSeconds(seconds: Long) {
        dataStore.edit { preferences ->
            val current = preferences[LISTENED_SECONDS] ?: 0L
            preferences[LISTENED_SECONDS] = current + seconds
        }
    }

    override val listenedCountries: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[LISTENED_COUNTRIES] ?: emptySet()
    }

    override suspend fun addListenedCountry(country: String) {
        dataStore.edit { preferences ->
            val current = preferences[LISTENED_COUNTRIES] ?: emptySet()
            preferences[LISTENED_COUNTRIES] = current + country
        }
    }
}

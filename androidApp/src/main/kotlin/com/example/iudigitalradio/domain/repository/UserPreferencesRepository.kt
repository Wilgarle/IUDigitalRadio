package com.example.iudigitalradio.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isVibrationEnabled: Flow<Boolean>
    suspend fun setVibrationEnabled(enabled: Boolean)

    val isDarkMode: Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean)

    val listenedSeconds: Flow<Long>
    suspend fun addListenedSeconds(seconds: Long)

    val listenedCountries: Flow<Set<String>>
    suspend fun addListenedCountry(country: String)
}

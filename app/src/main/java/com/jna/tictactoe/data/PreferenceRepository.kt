package com.jna.tictactoe.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferenceRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val NAME = stringPreferencesKey("user_name")
        val RANK = stringPreferencesKey("user_rank")
        val WIN_RATE = floatPreferencesKey("win_rate")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                name = preferences[PreferencesKeys.NAME] ?: "Player 1",
                rank = preferences[PreferencesKeys.RANK] ?: "Novice",
                winRate = preferences[PreferencesKeys.WIN_RATE] ?: 0f,
                soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true,
                darkModeEnabled = preferences[PreferencesKeys.DARK_MODE_ENABLED] ?: false
            )
        }

    suspend fun updateName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NAME] = name
        }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun updateDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun updateStats(rank: String, winRate: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.RANK] = rank
            preferences[PreferencesKeys.WIN_RATE] = winRate
        }
    }
}

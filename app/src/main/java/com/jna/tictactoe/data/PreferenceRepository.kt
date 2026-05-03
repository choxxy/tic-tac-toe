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
        val PROFILE_PICTURE_PATH = stringPreferencesKey("profile_picture_path")
        // Cumulative counters used to compute win rate and rank across all sessions.
        val TOTAL_WINS = intPreferencesKey("total_wins")
        val TOTAL_GAMES = intPreferencesKey("total_games")
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
                profilePicturePath = preferences[PreferencesKeys.PROFILE_PICTURE_PATH]
            )
        }

        suspend fun updateName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NAME] = name
        }
        }

        suspend fun updateProfilePicturePath(path: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROFILE_PICTURE_PATH] = path
        }
        }

    suspend fun updateStats(rank: String, winRate: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.RANK] = rank
            preferences[PreferencesKeys.WIN_RATE] = winRate
        }
    }

    /**
     * Records the outcome of a completed game and updates the player's win rate and rank.
     *
     * Increments the cumulative game counter and, if [won] is true, the win counter.
     * Win rate is recomputed as totalWins / totalGames. Rank is derived from win rate,
     * but requires at least 5 games before advancing past "Novice" to avoid early inflation.
     *
     * Rank thresholds:
     *   < 5 games         → Novice
     *   ≥ 90% win rate    → Master
     *   ≥ 75%             → Expert
     *   ≥ 60%             → Skilled
     *   ≥ 45%             → Intermediate
     *   ≥ 30%             → Beginner
     *   < 30%             → Novice
     */
    suspend fun recordGameResult(won: Boolean) {
        dataStore.edit { preferences ->
            val totalGames = (preferences[PreferencesKeys.TOTAL_GAMES] ?: 0) + 1
            val totalWins = (preferences[PreferencesKeys.TOTAL_WINS] ?: 0) + (if (won) 1 else 0)
            preferences[PreferencesKeys.TOTAL_GAMES] = totalGames
            preferences[PreferencesKeys.TOTAL_WINS] = totalWins

            val winRate = totalWins.toFloat() / totalGames
            val rank = when {
                totalGames < 5 -> "Novice"
                winRate >= 0.90f -> "Master"
                winRate >= 0.75f -> "Expert"
                winRate >= 0.60f -> "Skilled"
                winRate >= 0.45f -> "Intermediate"
                winRate >= 0.30f -> "Beginner"
                else -> "Novice"
            }
            preferences[PreferencesKeys.WIN_RATE] = winRate
            preferences[PreferencesKeys.RANK] = rank
        }
    }
}

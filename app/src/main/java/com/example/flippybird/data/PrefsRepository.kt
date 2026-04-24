package com.example.flippybird.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.flippybird.engine.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flippy_prefs")

/**
 * Persistent preferences via Jetpack DataStore.
 */
class PrefsRepository(private val context: Context) {

    companion object {
        val BEST_SCORE = intPreferencesKey("best_score")
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val SELECTED_SKIN = intPreferencesKey("selected_skin")
        val TARGET_FPS = intPreferencesKey("target_fps")
    }

    // Best Score
    val bestScore: Flow<Int> = context.dataStore.data.map { it[BEST_SCORE] ?: 0 }

    suspend fun setBestScore(score: Int) {
        context.dataStore.edit { it[BEST_SCORE] = score }
    }

    suspend fun resetBestScore() {
        context.dataStore.edit { it[BEST_SCORE] = 0 }
    }

    // Difficulty
    val difficulty: Flow<Difficulty> = context.dataStore.data.map {
        try { Difficulty.valueOf(it[DIFFICULTY] ?: "NORMAL") } catch (_: Exception) { Difficulty.NORMAL }
    }

    suspend fun setDifficulty(d: Difficulty) {
        context.dataStore.edit { it[DIFFICULTY] = d.name }
    }

    // Sound
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED] ?: true }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUND_ENABLED] = enabled }
    }

    // Music
    val musicEnabled: Flow<Boolean> = context.dataStore.data.map { it[MUSIC_ENABLED] ?: true }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { it[MUSIC_ENABLED] = enabled }
    }

    // Skin
    val selectedSkin: Flow<Int> = context.dataStore.data.map { it[SELECTED_SKIN] ?: 0 }

    suspend fun setSelectedSkin(index: Int) {
        context.dataStore.edit { it[SELECTED_SKIN] = index }
    }

    // Target FPS
    val targetFps: Flow<Int> = context.dataStore.data.map { it[TARGET_FPS] ?: 60 }

    suspend fun setTargetFps(fps: Int) {
        context.dataStore.edit { it[TARGET_FPS] = fps }
    }
}

package com.example.flippybird.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Game state enum with transition helpers.
 */
enum class GameState {
    MENU,
    READY,      // Waiting for first tap
    PLAYING,
    PAUSED,
    GAME_OVER;

    val isActive: Boolean get() = this == PLAYING || this == READY
}

/**
 * Manages game state transitions.
 */
class GameStateManager {
    var state by mutableStateOf(GameState.MENU)
        private set

    fun goToMenu() { state = GameState.MENU }
    fun ready() { state = GameState.READY }
    fun play() { state = GameState.PLAYING }
    fun pause() { if (state == GameState.PLAYING) state = GameState.PAUSED }
    fun resume() { if (state == GameState.PAUSED) state = GameState.PLAYING }
    fun gameOver() { state = GameState.GAME_OVER }

    fun togglePause() {
        when (state) {
            GameState.PLAYING -> pause()
            GameState.PAUSED -> resume()
            else -> {}
        }
    }
}

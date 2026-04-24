package com.example.flippybird.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flippybird.data.PrefsRepository
import com.example.flippybird.engine.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Central ViewModel that orchestrates all game engine modules and exposes
 * observable state for Compose UI.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    // Engine modules
    val bird = BirdController()
    val pipeManager = PipeManager()
    val collisionDetector = CollisionDetector()
    val stateManager = GameStateManager()
    val difficultyManager = DifficultyManager()
    val powerUpManager = PowerUpManager()
    val audioManager = AudioManager(application)

    // Data
    val prefs = PrefsRepository(application)

    // Observable state
    var score by mutableIntStateOf(0)
        private set
    var bestScore by mutableIntStateOf(0)
        private set
    var difficulty by mutableStateOf(Difficulty.NORMAL)
        private set
    var soundEnabled by mutableStateOf(true)
        private set
    var musicEnabled by mutableStateOf(true)
        private set
    var selectedSkin by mutableIntStateOf(0)
        private set
    var targetFps by mutableStateOf(60)
        private set

    // Screen dimensions (set by GameScreen on layout)
    var screenWidth by mutableFloatStateOf(0f)
    var screenHeight by mutableFloatStateOf(0f)
    val groundHeight: Float get() = screenHeight * 0.12f
    val groundY: Float get() = screenHeight - groundHeight

    // Game loop flag
    var isInitialized by mutableStateOf(false)
        private set

    init {
        audioManager.init()
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            bestScore = prefs.bestScore.first()
            difficulty = prefs.difficulty.first()
            soundEnabled = prefs.soundEnabled.first()
            musicEnabled = prefs.musicEnabled.first()
            selectedSkin = prefs.selectedSkin.first()
            targetFps = prefs.targetFps.first()

            audioManager.soundEnabled = soundEnabled
            audioManager.musicEnabled = musicEnabled
            bird.skinIndex = selectedSkin
        }
    }

    fun initGame(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height

        bird.init(width, height)
        pipeManager.init(width, height, groundHeight)
        powerUpManager.reset()
        score = 0
        stateManager.ready()
        isInitialized = true
    }

    fun onTap() {
        when (stateManager.state) {
            GameState.READY -> {
                stateManager.play()
                bird.flap()
                audioManager.playFlap()
                audioManager.startBgm()
            }
            GameState.PLAYING -> {
                bird.flap()
                audioManager.playFlap()
            }
            else -> {}
        }
    }

    // Observable state for frame updates
    var gameFrame by mutableLongStateOf(0L)
        private set

    fun update(dt: Float) {
        if (stateManager.state != GameState.PLAYING) return

        val clampedDt = dt.coerceAtMost(0.033f) // Cap at ~30fps minimum step

        // Update power-ups
        powerUpManager.update(clampedDt)

        // Get difficulty params
        val params = difficultyManager.getParams(score, difficulty)
        val effectiveSpeed = params.pipeSpeed * powerUpManager.speedMultiplier
        
        // Apply physics config
        bird.physics.configure(params.gravity, params.flapStrength)

        // Update bird
        bird.update(clampedDt)

        // Update pipes
        pipeManager.update(clampedDt, effectiveSpeed, params.gapSize, params.spawnInterval)

        // Check scoring
        val scored = collisionDetector.checkScoring(bird, pipeManager.activePipes)
        if (scored > 0) {
            score += scored
            audioManager.playScore()
            if (score > bestScore) {
                bestScore = score
                viewModelScope.launch { prefs.setBestScore(bestScore) }
            }
        }

        // Check power-up collection
        val collectedPipe = collisionDetector.checkPowerUpCollection(bird, pipeManager.activePipes)
        if (collectedPipe != null) {
            val type = if (kotlin.random.Random.nextBoolean()) PowerUpType.SHIELD else PowerUpType.SLOW_MOTION
            powerUpManager.activate(type)
        }

        // Check collisions
        val collision = collisionDetector.check(bird, pipeManager.activePipes, groundY, screenHeight)
        if (collision != CollisionType.NONE) {
            if (collision == CollisionType.PIPE && powerUpManager.consumeShield()) {
                // Shield absorbed the hit
            } else {
                audioManager.playHit()
                audioManager.stopBgm()
                stateManager.gameOver()
            }
        }

        // Clamp bird to screen
        if (bird.y < 0f) bird.y = 0f

        // Increment frame to trigger redraw
        gameFrame++
    }

    fun restart() {
        pipeManager.reset()
        powerUpManager.reset()
        score = 0
        bird.init(screenWidth, screenHeight)
        stateManager.ready()
    }

    fun goToMenu() {
        audioManager.stopBgm()
        pipeManager.reset()
        powerUpManager.reset()
        score = 0
        stateManager.goToMenu()
    }

    fun togglePause() {
        stateManager.togglePause()
        if (stateManager.state == GameState.PAUSED) {
            audioManager.stopBgm()
        } else if (stateManager.state == GameState.PLAYING) {
            audioManager.startBgm()
        }
    }

    fun setDifficultyLevel(d: Difficulty) {
        difficulty = d
        viewModelScope.launch { prefs.setDifficulty(d) }
    }

    fun setSoundToggle(enabled: Boolean) {
        soundEnabled = enabled
        audioManager.soundEnabled = enabled
        viewModelScope.launch { prefs.setSoundEnabled(enabled) }
    }

    fun setMusicToggle(enabled: Boolean) {
        musicEnabled = enabled
        audioManager.musicEnabled = enabled
        audioManager.updateMusicState()
        viewModelScope.launch { prefs.setMusicEnabled(enabled) }
    }

    fun selectSkin(index: Int) {
        selectedSkin = index
        bird.skinIndex = index
        viewModelScope.launch { prefs.setSelectedSkin(index) }
    }

    fun updateTargetFps(fps: Int) {
        targetFps = fps
        viewModelScope.launch { prefs.setTargetFps(fps) }
    }

    fun resetHighScore() {
        bestScore = 0
        viewModelScope.launch { prefs.resetBestScore() }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.release()
    }
}

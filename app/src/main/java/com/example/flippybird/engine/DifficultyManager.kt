package com.example.flippybird.engine

/**
 * Difficulty presets and dynamic scaling.
 */
enum class Difficulty {
    EASY,
    NORMAL,
    HARD
}

data class DifficultyParams(
    val pipeSpeed: Float,
    val gapSize: Float,
    val spawnInterval: Float,
    val gravity: Float,
    val flapStrength: Float
)

class DifficultyManager {

    companion object {
        // Base values per difficulty
        private val BASE_PARAMS = mapOf(
            Difficulty.EASY to DifficultyParams(
                pipeSpeed = 200f,
                gapSize = 300f,            // Wider gap
                spawnInterval = 3.2f,      // More time between pipes
                gravity = 2200f,           // Floatier
                flapStrength = -550f       // Gentler flap
            ),
            Difficulty.NORMAL to DifficultyParams(
                pipeSpeed = 280f,
                gapSize = 240f,
                spawnInterval = 2.4f,
                gravity = 2800f,           // Standard
                flapStrength = -650f
            ),
            Difficulty.HARD to DifficultyParams(
                pipeSpeed = 380f,
                gapSize = 190f,
                spawnInterval = 1.9f,
                gravity = 3400f,           // Heavier
                flapStrength = -700f       // Snappier flap
            )
        )

        // Scaling per point scored
        private const val SPEED_INCREMENT = 4f
        private const val GAP_DECREMENT = 1.5f
        private const val INTERVAL_DECREMENT = 0.01f

        private const val MIN_GAP = 140f
        private const val MIN_INTERVAL = 1.2f // Increased from 0.8
        private const val MAX_SPEED = 600f
    }

    fun getParams(score: Int, difficulty: Difficulty): DifficultyParams {
        val base = BASE_PARAMS[difficulty]!!
        return DifficultyParams(
            pipeSpeed = (base.pipeSpeed + score * SPEED_INCREMENT).coerceAtMost(MAX_SPEED),
            gapSize = (base.gapSize - score * GAP_DECREMENT).coerceAtLeast(MIN_GAP),
            spawnInterval = (base.spawnInterval - score * INTERVAL_DECREMENT).coerceAtLeast(MIN_INTERVAL),
            gravity = base.gravity,
            flapStrength = base.flapStrength
        )
    }
}

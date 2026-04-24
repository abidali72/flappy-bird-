package com.example.flippybird.engine

import kotlin.random.Random

/**
 * A single pipe obstacle with top and bottom columns and a gap.
 */
data class Pipe(
    var x: Float = 0f,
    var gapTop: Float = 0f,    // y of gap start (top of gap)
    var gapBottom: Float = 0f, // y of gap end (bottom of gap)
    var scored: Boolean = false,
    var active: Boolean = false,
    var width: Float = 80f,
    var hasPowerUp: Boolean = false,
    var powerUpCollected: Boolean = false
)

/**
 * Manages pipe spawning, movement, recycling (object pooling), and difficulty scaling.
 */
class PipeManager {
    private val pool = mutableListOf<Pipe>()
    val activePipes = mutableListOf<Pipe>()

    private var spawnTimer: Float = 0f
    private var screenWidth: Float = 0f
    private var screenHeight: Float = 0f
    private var groundY: Float = 0f

    companion object {
        const val POOL_SIZE = 10
        const val MIN_GAP_TOP = 0.15f   // fraction of screen
        const val MAX_GAP_BOTTOM = 0.75f // fraction of screen
        const val POWER_UP_CHANCE = 0.15f
    }

    fun init(screenW: Float, screenH: Float, groundHeight: Float) {
        screenWidth = screenW
        screenHeight = screenH
        groundY = screenH - groundHeight

        activePipes.clear()
        pool.clear()
        spawnTimer = 0f

        // Pre-fill pool
        repeat(POOL_SIZE) {
            pool.add(Pipe(width = screenW * 0.15f))
        }
    }

    private fun obtainPipe(): Pipe {
        return if (pool.isNotEmpty()) {
            pool.removeAt(pool.lastIndex)
        } else {
            Pipe(width = screenWidth * 0.15f)
        }
    }

    private fun recyclePipe(pipe: Pipe) {
        pipe.active = false
        pipe.scored = false
        pipe.hasPowerUp = false
        pipe.powerUpCollected = false
        pool.add(pipe)
    }

    fun update(dt: Float, speed: Float, gapSize: Float, spawnInterval: Float) {
        spawnTimer += dt

        if (spawnTimer >= spawnInterval) {
            spawnTimer -= spawnInterval
            spawnPipe(gapSize)
        }

        // Move pipes
        val dx = speed * dt
        val iterator = activePipes.iterator()
        while (iterator.hasNext()) {
            val pipe = iterator.next()
            pipe.x -= dx
            if (pipe.x + pipe.width < 0) {
                iterator.remove()
                recyclePipe(pipe)
            }
        }
    }

    private fun spawnPipe(gapSize: Float) {
        val pipe = obtainPipe()
        pipe.active = true
        pipe.x = screenWidth + 50f
        pipe.width = screenWidth * 0.15f

        val minGapTop = screenHeight * MIN_GAP_TOP
        val maxGapTop = groundY - gapSize - screenHeight * 0.1f

        pipe.gapTop = if (maxGapTop > minGapTop) {
            Random.nextFloat() * (maxGapTop - minGapTop) + minGapTop
        } else {
            minGapTop
        }
        pipe.gapBottom = pipe.gapTop + gapSize
        pipe.scored = false
        pipe.hasPowerUp = Random.nextFloat() < POWER_UP_CHANCE
        pipe.powerUpCollected = false

        activePipes.add(pipe)
    }

    fun reset() {
        activePipes.forEach { recyclePipe(it) }
        activePipes.clear()
        spawnTimer = 0f
    }
}

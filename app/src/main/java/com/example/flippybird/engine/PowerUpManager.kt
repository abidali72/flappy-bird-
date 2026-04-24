package com.example.flippybird.engine

/**
 * Power-up types available in the game.
 */
enum class PowerUpType {
    SHIELD,      // Absorbs one collision
    SLOW_MOTION  // Halves pipe speed for a duration
}

/**
 * Active power-up state.
 */
data class ActivePowerUp(
    val type: PowerUpType,
    var remainingTime: Float // seconds
)

/**
 * Manages power-up spawning, activation, and expiration.
 */
class PowerUpManager {

    companion object {
        const val SHIELD_DURATION = 999f  // Until hit
        const val SLOW_MOTION_DURATION = 5f
        const val SLOW_MOTION_FACTOR = 0.5f
    }

    private val activePowerUps = mutableListOf<ActivePowerUp>()

    val hasShield: Boolean
        get() = activePowerUps.any { it.type == PowerUpType.SHIELD }

    val isSlowMotion: Boolean
        get() = activePowerUps.any { it.type == PowerUpType.SLOW_MOTION }

    val speedMultiplier: Float
        get() = if (isSlowMotion) SLOW_MOTION_FACTOR else 1f

    fun activate(type: PowerUpType) {
        // Remove existing of same type
        activePowerUps.removeAll { it.type == type }

        val duration = when (type) {
            PowerUpType.SHIELD -> SHIELD_DURATION
            PowerUpType.SLOW_MOTION -> SLOW_MOTION_DURATION
        }
        activePowerUps.add(ActivePowerUp(type, duration))
    }

    fun consumeShield(): Boolean {
        val shield = activePowerUps.find { it.type == PowerUpType.SHIELD }
        return if (shield != null) {
            activePowerUps.remove(shield)
            true
        } else false
    }

    fun update(dt: Float) {
        val iterator = activePowerUps.iterator()
        while (iterator.hasNext()) {
            val powerUp = iterator.next()
            powerUp.remainingTime -= dt
            if (powerUp.remainingTime <= 0f) {
                iterator.remove()
            }
        }
    }

    fun getActivePowerUps(): List<ActivePowerUp> = activePowerUps.toList()

    fun reset() {
        activePowerUps.clear()
    }
}

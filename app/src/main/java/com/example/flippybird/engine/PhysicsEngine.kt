package com.example.flippybird.engine

/**
 * Core physics engine handling gravity and bird movement.
 */
class PhysicsEngine {
    companion object {
        const val TERMINAL_VELOCITY = 1200f
        const val ROTATION_SPEED = 300f // degrees/s for downward tilt
        const val FLAP_ROTATION = -30f  // degrees on flap
    }

    var gravity: Float = 2800f
    var flapVelocity: Float = -650f

    // Configurable via difficulty
    fun configure(g: Float, fv: Float) {
        gravity = g
        flapVelocity = fv
    }

    var velocity: Float = 0f
        private set

    var rotation: Float = 0f
        private set

    fun update(dt: Float) {
        velocity += gravity * dt
        if (velocity > TERMINAL_VELOCITY) velocity = TERMINAL_VELOCITY

        // Rotate bird based on velocity
        rotation = if (velocity < 0) {
            FLAP_ROTATION
        } else {
            (rotation + ROTATION_SPEED * dt).coerceAtMost(90f)
        }
    }

    fun flap() {
        velocity = flapVelocity
        rotation = FLAP_ROTATION
    }

    fun reset() {
        velocity = 0f
        rotation = 0f
    }

    fun getDisplacement(dt: Float): Float {
        return velocity * dt + 0.5f * gravity * dt * dt
    }
}

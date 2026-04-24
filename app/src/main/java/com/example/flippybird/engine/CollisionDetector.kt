package com.example.flippybird.engine

/**
 * Result of a collision check.
 */
enum class CollisionType {
    NONE,
    PIPE,
    GROUND,
    CEILING
}

/**
 * Detects collisions between the bird and pipes, ground, or ceiling.
 */
class CollisionDetector {

    /**
     * Check for collision. Uses AABB (axis-aligned bounding box) with slight inset
     * for forgiveness.
     */
    fun check(
        bird: BirdController,
        pipes: List<Pipe>,
        groundY: Float,
        screenHeight: Float
    ): CollisionType {
        // Inset for more forgiving hitbox (80% of actual size)
        val inset = bird.width * 0.1f
        val birdLeft = bird.x + inset
        val birdRight = bird.x + bird.width - inset
        val birdTop = bird.y + inset
        val birdBottom = bird.y + bird.height - inset

        // Ground collision
        if (birdBottom >= groundY) return CollisionType.GROUND

        // Ceiling collision
        if (birdTop <= 0f) return CollisionType.CEILING

        // Pipe collisions
        for (pipe in pipes) {
            if (!pipe.active) continue

            val pipeLeft = pipe.x
            val pipeRight = pipe.x + pipe.width

            // Check horizontal overlap first
            if (birdRight > pipeLeft && birdLeft < pipeRight) {
                // Top pipe collision (above gap)
                if (birdTop < pipe.gapTop) return CollisionType.PIPE
                // Bottom pipe collision (below gap)
                if (birdBottom > pipe.gapBottom) return CollisionType.PIPE
            }
        }

        return CollisionType.NONE
    }

    /**
     * Check if the bird has passed a pipe (for scoring).
     */
    fun checkScoring(bird: BirdController, pipes: List<Pipe>): Int {
        var scored = 0
        for (pipe in pipes) {
            if (!pipe.scored && pipe.active) {
                if (bird.x > pipe.x + pipe.width) {
                    pipe.scored = true
                    scored++
                }
            }
        }
        return scored
    }

    /**
     * Check if bird collected a power-up from a pipe.
     */
    fun checkPowerUpCollection(bird: BirdController, pipes: List<Pipe>): Pipe? {
        val birdCenterX = bird.centerX
        val birdCenterY = bird.centerY
        val collectRadius = bird.width

        for (pipe in pipes) {
            if (pipe.hasPowerUp && !pipe.powerUpCollected && pipe.active) {
                val powerUpX = pipe.x + pipe.width / 2f
                val powerUpY = (pipe.gapTop + pipe.gapBottom) / 2f

                val dx = birdCenterX - powerUpX
                val dy = birdCenterY - powerUpY
                if (dx * dx + dy * dy < collectRadius * collectRadius) {
                    pipe.powerUpCollected = true
                    return pipe
                }
            }
        }
        return null
    }
}

package com.example.flippybird.engine

/**
 * Controls the bird's position, animation, and skin.
 */
class BirdController {
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 50f
    var height: Float = 38f

    val physics = PhysicsEngine()

    // Animation
    var wingFrame: Int = 0
        private set
    private var wingTimer: Float = 0f
    private val wingInterval = 0.08f
    private val wingFrameCount = 3

    // Skin
    var skinIndex: Int = 0

    fun init(screenWidth: Float, screenHeight: Float) {
        x = screenWidth * 0.25f
        y = screenHeight * 0.45f
        width = screenWidth * 0.1f
        height = width * 0.75f
        physics.reset()
        wingFrame = 0
        wingTimer = 0f
    }

    fun update(dt: Float) {
        physics.update(dt)
        y += physics.getDisplacement(dt)

        // Wing animation
        wingTimer += dt
        if (wingTimer >= wingInterval) {
            wingTimer -= wingInterval
            wingFrame = (wingFrame + 1) % wingFrameCount
        }
    }

    fun flap() {
        physics.flap()
        wingFrame = 0
        wingTimer = 0f
    }

    fun reset(screenWidth: Float, screenHeight: Float) {
        init(screenWidth, screenHeight)
    }

    val rotation: Float get() = physics.rotation
    val centerX: Float get() = x + width / 2f
    val centerY: Float get() = y + height / 2f
}

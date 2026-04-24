package com.example.flippybird.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flippybird.engine.*
import com.example.flippybird.ui.theme.*
import com.example.flippybird.viewmodel.GameViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToMenu: () -> Unit
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // Reset game state when entering the screen
    LaunchedEffect(Unit) {
        if (viewModel.isInitialized) {
            viewModel.restart()
        }
    }

    // Game loop
    LaunchedEffect(viewModel.stateManager.state, viewModel.targetFps) {
        if (viewModel.stateManager.state == GameState.PLAYING) {
            var lastTime = withFrameNanos { it }
            val targetFrameTime = 1_000_000_000L / viewModel.targetFps

            while (viewModel.stateManager.state == GameState.PLAYING) {
                val currentTime = withFrameNanos { it }
                val dt = (currentTime - lastTime) / 1_000_000_000f
                
                if (currentTime - lastTime >= targetFrameTime) {
                    lastTime = currentTime
                    viewModel.update(dt)
                }
            }
        }
    }

    // Cache objects to avoid allocation in onDraw
    val hillPath = remember { Path() }
    val birdWingPath = remember { Path() }
    val birdBeakPath = remember { Path() }

    // Pre-calculate colors and brushes that don't depend on per-frame state
    val skyBrush = remember(viewModel.groundY) {
        Brush.verticalGradient(
            colors = listOf(SkyTop, SkyBottom),
            startY = 0f,
            endY = viewModel.groundY
        )
    }
    
    val groundBrush = remember(viewModel.groundY, viewModel.screenHeight) {
        Brush.verticalGradient(
            colors = listOf(GroundGreen, GroundDark),
            startY = viewModel.groundY,
            endY = viewModel.screenHeight
        )
    }

    val pipeBorderColor = remember { Color(0xFF2E7D32) } // Darker green for outline
    val pipeBorderWidth = 4f
    
    // 3D Cylinder Gradient: Dark -> Light -> Highlight -> Light -> Dark
    val pipeGradientColors = remember {
        listOf(
            Color(0xFF2E7D32), // Dark Shadow
            Color(0xFF43A047), // Medium
            Color(0xFF81C784), // Highlight (Shiny)
            Color(0xFF43A047), // Medium
            Color(0xFF2E7D32)  // Dark Shadow
        )
    }

    // Cached colors
    val hillColor = remember { Color(0xFF81C784).copy(alpha = 0.5f) }
    val shieldGlowColor = remember { ShieldBlue.copy(alpha = 0.3f) }
    val shieldBorderColor = remember { ShieldBlue.copy(alpha = 0.5f) }
    val shadowColor = remember { Color.Black.copy(alpha = 0.4f) }
    val tapTextColor = remember { Color.White.copy(alpha = 0.9f) }
    val groundLineColor = remember { GroundSand.copy(alpha = 0.2f) }
    val grassEdgeColor = remember { Color(0xFF66BB6A) }
    val beakColor = remember { Color(0xFFFF5722) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.onTap()
            }
            .onSizeChanged {
                if (!viewModel.isInitialized || viewModel.screenWidth != it.width.toFloat()) {
                    viewModel.initGame(it.width.toFloat(), it.height.toFloat())
                }
            }
    ) {
        // Main game canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val frame = viewModel.gameFrame // Force redraw on tick

            val w = size.width
            val h = size.height
            val groundY = viewModel.groundY
            val groundH = viewModel.groundHeight

            // -- Sky gradient --
            drawRect(brush = skyBrush)

            // -- Background hills --
            hillPath.rewind()
            hillPath.moveTo(0f, groundY)
            val hillCount = 5
            val segWidth = w / hillCount
            for (i in 0 until hillCount) {
                val x = i * segWidth
                val hillH = groundY - (h * 0.05f + h * 0.03f * sin(i * 1.5f))
                hillPath.quadraticBezierTo(
                    x + segWidth * 0.5f, hillH,
                    x + segWidth, groundY
                )
            }
            hillPath.lineTo(w, groundY)
            hillPath.close()
            drawPath(hillPath, hillColor)

            // -- Pipes --
            val pipes = viewModel.pipeManager.activePipes // Access list once
            for (i in pipes.indices) {
                val pipe = pipes[i]
                if (!pipe.active) continue

                val pipeLeft = pipe.x
                val pipeRight = pipe.x + pipe.width
                val capHeight = pipe.width * 0.4f // Taller cap
                val capOverhang = pipe.width * 0.1f // Slightly wider than body

                // -- Top Pipe --
                val topPipeBottom = pipe.gapTop
                val topBodyBottom = topPipeBottom - capHeight
                
                // 1. Top Body Border
                drawRect(
                    color = pipeBorderColor,
                    topLeft = Offset(pipeLeft - pipeBorderWidth, 0f),
                    size = Size(pipe.width + pipeBorderWidth * 2, topBodyBottom)
                )
                // 2. Top Body Fill
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = pipeGradientColors,
                        startX = pipeLeft,
                        endX = pipeRight
                    ),
                    topLeft = Offset(pipeLeft, 0f),
                    size = Size(pipe.width, topBodyBottom)
                )
                
                // 3. Top Cap Border
                drawRect(
                    color = pipeBorderColor,
                    topLeft = Offset(pipeLeft - capOverhang - pipeBorderWidth, topBodyBottom),
                    size = Size(pipe.width + capOverhang * 2 + pipeBorderWidth * 2, capHeight + pipeBorderWidth)
                )
                // 4. Top Cap Fill
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = pipeGradientColors,
                        startX = pipeLeft - capOverhang,
                        endX = pipeRight + capOverhang
                    ),
                    topLeft = Offset(pipeLeft - capOverhang, topBodyBottom),
                    size = Size(pipe.width + capOverhang * 2, capHeight)
                )
                // 5. Top Cap Detail (Lip shadow)
                drawRect(
                    color = Color.Black.copy(alpha = 0.2f),
                    topLeft = Offset(pipeLeft - capOverhang, topPipeBottom - capHeight * 0.1f),
                    size = Size(pipe.width + capOverhang * 2, capHeight * 0.1f)
                )


                // -- Bottom Pipe --
                val bottomPipeTop = pipe.gapBottom
                val bottomBodyTop = bottomPipeTop + capHeight

                // 1. Bottom Body Border
                drawRect(
                    color = pipeBorderColor,
                    topLeft = Offset(pipeLeft - pipeBorderWidth, bottomBodyTop),
                    size = Size(pipe.width + pipeBorderWidth * 2, groundY - bottomBodyTop)
                )
                // 2. Bottom Body Fill
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = pipeGradientColors,
                        startX = pipeLeft,
                        endX = pipeRight
                    ),
                    topLeft = Offset(pipeLeft, bottomBodyTop),
                    size = Size(pipe.width, groundY - bottomBodyTop)
                )
                
                // 3. Bottom Cap Border
                drawRect(
                    color = pipeBorderColor,
                    topLeft = Offset(pipeLeft - capOverhang - pipeBorderWidth, bottomPipeTop - pipeBorderWidth),
                    size = Size(pipe.width + capOverhang * 2 + pipeBorderWidth * 2, capHeight + pipeBorderWidth)
                )
                // 4. Bottom Cap Fill
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = pipeGradientColors,
                        startX = pipeLeft - capOverhang,
                        endX = pipeRight + capOverhang
                    ),
                    topLeft = Offset(pipeLeft - capOverhang, bottomPipeTop),
                    size = Size(pipe.width + capOverhang * 2, capHeight)
                )
                // 5. Bottom Cap Detail (Lip shadow)
                drawRect(
                    color = Color.Black.copy(alpha = 0.2f),
                    topLeft = Offset(pipeLeft - capOverhang, bottomPipeTop + capHeight),
                    size = Size(pipe.width + capOverhang * 2, capHeight * 0.1f) // Shadow below cap
                )

                // Power-up indicator in gap
                if (pipe.hasPowerUp && !pipe.powerUpCollected) {
                    val puX = pipe.x + pipe.width / 2f
                    val puY = (pipe.gapTop + pipe.gapBottom) / 2f
                    val puRadius = pipe.width * 0.2f

                    // Glow
                    drawCircle(
                        color = shieldGlowColor,
                        radius = puRadius * 1.6f,
                        center = Offset(puX, puY)
                    )
                    // Star shape
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AccentGold, SlowMotionAmber),
                            center = Offset(puX, puY),
                            radius = puRadius
                        ),
                        radius = puRadius,
                        center = Offset(puX, puY)
                    )
                    // Inner dot
                    drawCircle(
                        color = Color.White,
                        radius = puRadius * 0.35f,
                        center = Offset(puX, puY)
                    )
                }
            }

            // -- Ground --
            drawRect(
                brush = groundBrush,
                topLeft = Offset(0f, groundY),
                size = Size(w, groundH)
            )
            // Grass edge
            drawRect(
                color = grassEdgeColor,
                topLeft = Offset(0f, groundY - h * 0.005f),
                size = Size(w, h * 0.012f)
            )
            // Ground texture lines
            for (i in 0 until 20) {
                val gx = (i.toFloat() / 20f) * w
                drawLine(
                    color = groundLineColor,
                    start = Offset(gx, groundY + groundH * 0.3f),
                    end = Offset(gx + w * 0.03f, groundY + groundH * 0.7f),
                    strokeWidth = 2f
                )
            }

            // -- Bird --
            val bird = viewModel.bird
            val skinPair = BirdSkinColors.getOrElse(bird.skinIndex) { BirdSkinColors[0] }
            val bodyColor = skinPair.first
            val wingColor = skinPair.second

            // Optimize bird rotation by transforming canvas
            withTransform({
                rotate(degrees = bird.rotation, pivot = Offset(bird.centerX, bird.centerY))
                translate(left = bird.x, top = bird.y)
            }) {
                // Drawing at (0,0) relative to bird's top-left
                val birdW = bird.width
                val birdH = bird.height

                // Body
                drawOval(
                    color = bodyColor,
                    topLeft = Offset.Zero,
                    size = Size(birdW, birdH)
                )

                // Belly
                drawOval(
                    color = bodyColor.copy(alpha = 0.5f).compositeOver(Color.White), // Could be optimized further but fine
                    topLeft = Offset(birdW * 0.2f, birdH * 0.35f),
                    size = Size(birdW * 0.5f, birdH * 0.5f)
                )

                // Wing
                val wingY = birdH * 0.3f + when (bird.wingFrame) {
                    0 -> 0f
                    1 -> -birdH * 0.15f
                    else -> birdH * 0.1f
                }
                
                birdWingPath.rewind()
                birdWingPath.moveTo(birdW * 0.15f, birdH * 0.5f)
                birdWingPath.quadraticBezierTo(
                    -birdW * 0.1f, wingY,
                    birdW * 0.3f, birdH * 0.3f
                )
                birdWingPath.close()
                drawPath(birdWingPath, wingColor)

                // Eye
                drawCircle(
                    color = Color.White,
                    radius = birdW * 0.14f,
                    center = Offset(birdW * 0.7f, birdH * 0.3f)
                )
                drawCircle(
                    color = Color.Black,
                    radius = birdW * 0.07f,
                    center = Offset(birdW * 0.73f, birdH * 0.3f)
                )

                // Beak
                birdBeakPath.rewind()
                birdBeakPath.moveTo(birdW * 0.85f, birdH * 0.4f)
                birdBeakPath.lineTo(birdW * 1.15f, birdH * 0.5f)
                birdBeakPath.lineTo(birdW * 0.85f, birdH * 0.6f)
                birdBeakPath.close()
                drawPath(birdBeakPath, beakColor)

                // Shield effect
                if (viewModel.powerUpManager.hasShield) {
                    val centerBird = Offset(birdW / 2, birdH / 2)
                    drawCircle(
                        color = shieldGlowColor, // cached
                        radius = birdW * 0.8f,
                        center = centerBird
                    )
                    drawCircle(
                        color = shieldBorderColor, // cached
                        radius = birdW * 0.8f,
                        center = centerBird,
                        style = Stroke(width = 3f)
                    )
                }
            }

            // -- Score display --
            if (viewModel.stateManager.state != GameState.MENU) {
                val scoreText = viewModel.score.toString()
                val scoreStyle = TextStyle(
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                val measured = textMeasurer.measure(scoreText, scoreStyle)
                val scoreX = (w - measured.size.width) / 2f
                val scoreY = h * 0.06f

                // Shadow
                drawText(
                    textMeasurer = textMeasurer,
                    text = scoreText,
                    style = scoreStyle.copy(color = shadowColor),
                    topLeft = Offset(scoreX + 4f, scoreY + 4f)
                )
                // Main text
                drawText(
                    textMeasurer = textMeasurer,
                    text = scoreText,
                    style = scoreStyle,
                    topLeft = Offset(scoreX, scoreY)
                )
            }

            // -- "Tap to Start" text --
            if (viewModel.stateManager.state == GameState.READY) {
                val tapText = "TAP TO START"
                val tapStyle = TextStyle(
                    color = tapTextColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                val tapMeasured = textMeasurer.measure(tapText, tapStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = tapText,
                    style = tapStyle,
                    topLeft = Offset(
                        (w - tapMeasured.size.width) / 2f,
                        h * 0.65f
                    )
                )
            }

            // -- Active power-up indicators --
            // Re-fetch list only if it changes (observable list in VM?) 
            // Currently it returns a new list every call. Ideally optimize VM, but here:
            val activePowerUps = viewModel.powerUpManager.getActivePowerUps()
            activePowerUps.forEachIndexed { index, pu ->
                val iconY = h * 0.15f + index * (h * 0.06f)
                val iconX = w * 0.05f
                val puColor = when (pu.type) {
                    PowerUpType.SHIELD -> ShieldBlue
                    PowerUpType.SLOW_MOTION -> SlowMotionAmber
                }
                drawRoundRect(
                    color = puColor.copy(alpha = 0.3f), // Optimize if possible, but fine for UI overlay
                    topLeft = Offset(iconX, iconY),
                    size = Size(w * 0.2f, h * 0.04f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                val barWidth = if (pu.type == PowerUpType.SHIELD) w * 0.2f
                else (pu.remainingTime / 5f).coerceIn(0f, 1f) * w * 0.2f
                drawRoundRect(
                    color = puColor,
                    topLeft = Offset(iconX, iconY),
                    size = Size(barWidth, h * 0.04f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                val label = when (pu.type) {
                    PowerUpType.SHIELD -> "🛡️"
                    PowerUpType.SLOW_MOTION -> "🕐"
                }
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    style = TextStyle(fontSize = 16.sp),
                    topLeft = Offset(iconX + 4f, iconY + 2f)
                )
            }

            // -- Slow motion overlay --
            if (viewModel.powerUpManager.isSlowMotion) {
                drawRect(
                    color = SlowMotionAmber.copy(alpha = 0.08f),
                    size = size
                )
            }
        }

        // Pause button
        if (viewModel.stateManager.state == GameState.PLAYING || viewModel.stateManager.state == GameState.PAUSED) {
            IconButton(
                onClick = { viewModel.togglePause() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        DarkBg.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (viewModel.stateManager.state == GameState.PAUSED)
                        Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = "Pause/Resume",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Pause overlay
        if (viewModel.stateManager.state == GameState.PAUSED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "PAUSED",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Button(
                        onClick = { viewModel.togglePause() },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text("RESUME", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.goToMenu()
                            onNavigateToMenu()
                        },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("MAIN MENU", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Game Over overlay
        if (viewModel.stateManager.state == GameState.GAME_OVER) {
            GameOverOverlay(
                score = viewModel.score,
                bestScore = viewModel.bestScore,
                onRestart = { viewModel.restart() },
                onHome = {
                    viewModel.goToMenu()
                    onNavigateToMenu()
                }
            )
        }
    }
}

package com.example.flippybird.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flippybird.ui.theme.*
import kotlin.math.sin

@Composable
fun MainMenuScreen(
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSkinsClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "menu_anim")

    // Cloud animation offset
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud"
    )

    // Bird floating animation
    val birdFloat by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bird_float"
    )

    // Title pulse
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "title_pulse"
    )

    // Button entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated sky background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Sky gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(SkyTop, SkyBottom),
                    startY = 0f,
                    endY = h * 0.85f
                )
            )

            // Ground
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(GroundGreen, GroundDark)
                ),
                topLeft = Offset(0f, h * 0.85f),
                size = androidx.compose.ui.geometry.Size(w, h * 0.15f)
            )

            // Grass line
            drawRect(
                color = Color(0xFF66BB6A),
                topLeft = Offset(0f, h * 0.845f),
                size = androidx.compose.ui.geometry.Size(w, h * 0.015f)
            )

            // Clouds
            val cloudPositions = listOf(0.1f, 0.35f, 0.65f, 0.85f)
            val cloudYs = listOf(0.1f, 0.2f, 0.08f, 0.18f)
            for (i in cloudPositions.indices) {
                val cx = ((cloudPositions[i] + cloudOffset) % 1.2f - 0.1f) * w
                val cy = cloudYs[i] * h
                val cloudRadius = w * 0.06f

                drawCircle(Color.White.copy(alpha = 0.7f), cloudRadius, Offset(cx, cy))
                drawCircle(Color.White.copy(alpha = 0.7f), cloudRadius * 0.75f, Offset(cx - cloudRadius * 0.7f, cy + cloudRadius * 0.1f))
                drawCircle(Color.White.copy(alpha = 0.7f), cloudRadius * 0.85f, Offset(cx + cloudRadius * 0.6f, cy + cloudRadius * 0.15f))
            }

            // Animated bird on menu
            val birdX = w * 0.5f
            val birdY = h * 0.32f + birdFloat
            val birdSize = w * 0.12f

            // Body
            drawCircle(BirdYellow, birdSize * 0.5f, Offset(birdX, birdY))

            // Wing
            val wingPath = Path().apply {
                moveTo(birdX - birdSize * 0.3f, birdY)
                quadraticBezierTo(
                    birdX - birdSize * 0.7f, birdY - birdSize * 0.3f,
                    birdX - birdSize * 0.2f, birdY - birdSize * 0.1f
                )
                close()
            }
            drawPath(wingPath, BirdOrange, style = Fill)

            // Eye
            drawCircle(Color.White, birdSize * 0.15f, Offset(birdX + birdSize * 0.15f, birdY - birdSize * 0.12f))
            drawCircle(Color.Black, birdSize * 0.07f, Offset(birdX + birdSize * 0.18f, birdY - birdSize * 0.12f))

            // Beak
            val beakPath = Path().apply {
                moveTo(birdX + birdSize * 0.35f, birdY - birdSize * 0.05f)
                lineTo(birdX + birdSize * 0.58f, birdY + birdSize * 0.05f)
                lineTo(birdX + birdSize * 0.35f, birdY + birdSize * 0.12f)
                close()
            }
            drawPath(beakPath, Color(0xFFFF5722), style = Fill)
        }

        // UI overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 60.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FLIPPY",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 4.sp
                    ),
                    modifier = Modifier.scale(titleScale),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "BIRD",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGold,
                        letterSpacing = 8.sp
                    ),
                    modifier = Modifier.scale(titleScale),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.weight(1f))

            // Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                // Play button
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen
                    ),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text(
                        "▶  PLAY",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }

                // Settings button
                OutlinedButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 2.dp,
                    )
                ) {
                    Text(
                        "⚙  SETTINGS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }

                // Skins button
                OutlinedButton(
                    onClick = onSkinsClick,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentGold
                    )
                ) {
                    Text(
                        "🐦  SKINS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

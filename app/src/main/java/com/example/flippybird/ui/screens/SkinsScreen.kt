package com.example.flippybird.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flippybird.ui.theme.*
import com.example.flippybird.viewmodel.GameViewModel

@Composable
fun SkinsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val selectedSkin = viewModel.selectedSkin

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NightSkyTop, NightSkyBottom)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    "BIRD SKINS",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 3.sp
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Select your favorite bird skin",
                style = MaterialTheme.typography.bodyLarge.copy(color = TextGray)
            )

            Spacer(Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(BirdSkinColors) { index, skinPair ->
                    val isSelected = index == selectedSkin
                    val bodyColor = skinPair.first
                    val wingColor = skinPair.second
                    val skinName = BirdSkinNames.getOrElse(index) { "Skin $index" }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.85f)
                            .then(
                                if (isSelected) Modifier.border(
                                    3.dp, AccentGold, RoundedCornerShape(20.dp)
                                ) else Modifier
                            )
                            .clickable { viewModel.selectSkin(index) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CardBg.copy(alpha = 0.9f) else CardBg
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isSelected) 12.dp else 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Bird preview
                            Canvas(
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(8.dp)
                            ) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val birdSize = size.width * 0.4f

                                // Body
                                drawCircle(bodyColor, birdSize, Offset(cx, cy))

                                // Belly
                                drawCircle(
                                    bodyColor.copy(alpha = 0.5f),
                                    birdSize * 0.6f,
                                    Offset(cx, cy + birdSize * 0.15f)
                                )

                                // Wing
                                val wingPath = Path().apply {
                                    moveTo(cx - birdSize * 0.6f, cy)
                                    quadraticBezierTo(
                                        cx - birdSize * 1.1f, cy - birdSize * 0.5f,
                                        cx - birdSize * 0.2f, cy - birdSize * 0.3f
                                    )
                                    close()
                                }
                                drawPath(wingPath, wingColor, style = Fill)

                                // Eye
                                drawCircle(Color.White, birdSize * 0.2f, Offset(cx + birdSize * 0.35f, cy - birdSize * 0.2f))
                                drawCircle(Color.Black, birdSize * 0.1f, Offset(cx + birdSize * 0.4f, cy - birdSize * 0.2f))

                                // Beak
                                val beakPath = Path().apply {
                                    moveTo(cx + birdSize * 0.7f, cy - birdSize * 0.1f)
                                    lineTo(cx + birdSize * 1.1f, cy + birdSize * 0.05f)
                                    lineTo(cx + birdSize * 0.7f, cy + birdSize * 0.18f)
                                    close()
                                }
                                drawPath(beakPath, Color(0xFFFF5722), style = Fill)
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                skinName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AccentGold else Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            if (isSelected) {
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(AccentGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

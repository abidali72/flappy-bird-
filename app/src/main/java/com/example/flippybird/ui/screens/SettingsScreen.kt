package com.example.flippybird.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flippybird.engine.Difficulty
import com.example.flippybird.ui.theme.*
import com.example.flippybird.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val difficulty = viewModel.difficulty
    val soundEnabled = viewModel.soundEnabled
    val musicEnabled = viewModel.musicEnabled

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
                    "SETTINGS",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 3.sp
                    )
                )
            }

            Spacer(Modifier.height(32.dp))

            // Difficulty Section
            SettingSectionTitle("DIFFICULTY")
            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Difficulty.entries.forEach { diff ->
                    val isSelected = difficulty == diff
                    val color = when (diff) {
                        Difficulty.EASY -> AccentGreen
                        Difficulty.NORMAL -> AccentGold
                        Difficulty.HARD -> Color(0xFFEF5350)
                    }

                    Button(
                        onClick = { viewModel.setDifficultyLevel(diff) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) color else CardBg,
                            contentColor = if (isSelected) Color.White else TextGray
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (isSelected) 8.dp else 0.dp
                        )
                    ) {
                        Text(
                            diff.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Audio Section
            SettingSectionTitle("AUDIO")
            Spacer(Modifier.height(12.dp))

            SettingToggleRow(
                title = "Sound Effects",
                emoji = "🔊",
                checked = soundEnabled,
                onToggle = { viewModel.setSoundToggle(it) }
            )

            Spacer(Modifier.height(12.dp))

            SettingToggleRow(
                title = "Background Music",
                emoji = "🎵",
                checked = musicEnabled,
                onToggle = { viewModel.setMusicToggle(it) }
            )

            Spacer(Modifier.height(32.dp))

            // FPS Section
            SettingSectionTitle("PERFORMANCE")
            Spacer(Modifier.height(12.dp))

            Text(
                "Target FPS",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(60, 90, 120).forEach { fps ->
                    val isSelected = viewModel.targetFps == fps
                    Button(
                        onClick = { viewModel.updateTargetFps(fps) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) AccentGreen else CardBg,
                            contentColor = if (isSelected) Color.White else TextGray
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (isSelected) 8.dp else 0.dp
                        )
                    ) {
                        Text(
                            "$fps FPS",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Data Section
            SettingSectionTitle("DATA")
            Spacer(Modifier.height(12.dp))

            var showConfirm by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Reset High Score",
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                        )
                        Text(
                            "Current Best: ${viewModel.bestScore}",
                            style = MaterialTheme.typography.bodyLarge.copy(color = TextGray)
                        )
                    }
                    Button(
                        onClick = {
                            if (showConfirm) {
                                viewModel.resetHighScore()
                                showConfirm = false
                            } else {
                                showConfirm = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showConfirm) Color(0xFFEF5350) else CardBg
                        )
                    ) {
                        Text(
                            if (showConfirm) "CONFIRM" else "RESET",
                            fontWeight = FontWeight.Bold,
                            color = if (showConfirm) Color.White else Color(0xFFEF5350)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge.copy(
            color = AccentGold,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun SettingToggleRow(
    title: String,
    emoji: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(emoji, fontSize = 24.sp)
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentGreen,
                    checkedTrackColor = AccentGreen.copy(alpha = 0.3f)
                )
            )
        }
    }
}

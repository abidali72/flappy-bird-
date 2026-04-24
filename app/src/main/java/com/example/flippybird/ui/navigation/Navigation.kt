package com.example.flippybird.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.flippybird.ui.screens.*
import com.example.flippybird.viewmodel.GameViewModel

object Routes {
    const val MENU = "menu"
    const val GAME = "game"
    const val SETTINGS = "settings"
    const val SKINS = "skins"
}

@Composable
fun FlippyNavGraph(
    navController: NavHostController,
    viewModel: GameViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MENU
    ) {
        composable(Routes.MENU) {
            MainMenuScreen(
                onPlayClick = {
                    navController.navigate(Routes.GAME) {
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                },
                onSkinsClick = {
                    navController.navigate(Routes.SKINS)
                }
            )
        }

        composable(Routes.GAME) {
            GameScreen(
                viewModel = viewModel,
                onNavigateToMenu = {
                    navController.navigate(Routes.MENU) {
                        popUpTo(Routes.MENU) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SKINS) {
            SkinsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

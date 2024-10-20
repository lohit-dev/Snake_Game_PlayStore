package com.king_grey.retro_snake.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.king_grey.retro_snake.data.Game
import com.king_grey.retro_snake.ui.screens.GameScreen
import com.king_grey.retro_snake.ui.screens.OnBoardingScreen

@Composable
fun SetUpNavigation(navController: NavHostController, game: Game) {

    NavHost(navController, startDestination = Screen.OnBoardingScreen.route) {

        composable(route = Screen.OnBoardingScreen.route) {
            OnBoardingScreen(navController)
        }

        composable(route = Screen.GameScreen.route) {
            GameScreen(game = game)
        }
    }
}
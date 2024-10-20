package com.king_grey.retro_snake.ui.navigation

sealed class Screen(val route: String) {
    object OnBoardingScreen : Screen("onboarding")
    object GameScreen : Screen("snake_game")
}
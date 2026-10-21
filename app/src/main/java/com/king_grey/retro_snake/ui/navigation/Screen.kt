package com.king_grey.retro_snake.ui.navigation

sealed class Screen(val route: String) {
    data object Menu : Screen("menu")
    data object Game : Screen("game")
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
    data object Help : Screen("help")
}

package com.king_grey.retro_snake.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.king_grey.retro_snake.ui.GameViewModel
import com.king_grey.retro_snake.ui.screens.GameScreen
import com.king_grey.retro_snake.ui.screens.HelpScreen
import com.king_grey.retro_snake.ui.screens.MenuScreen
import com.king_grey.retro_snake.ui.screens.SettingsScreen
import com.king_grey.retro_snake.ui.screens.StatsScreen

@Composable
fun SetUpNavigation(navController: NavHostController, vm: GameViewModel) {
    val goBack: () -> Unit = { navController.popBackStack() }
    // Hard cuts between screens, like switching a console screen.
    NavHost(
        navController = navController,
        startDestination = Screen.Menu.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(Screen.Menu.route) {
            MenuScreen(
                vm = vm,
                onPlay = {
                    vm.startNewGame()
                    navController.navigate(Screen.Game.route) { launchSingleTop = true }
                },
                onStats = { navController.navigate(Screen.Stats.route) { launchSingleTop = true } },
                onSettings = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } },
                onHelp = { navController.navigate(Screen.Help.route) { launchSingleTop = true } },
            )
        }
        composable(Screen.Game.route) {
            GameScreen(
                vm = vm,
                onExit = {
                    vm.quit()
                    navController.popBackStack(Screen.Menu.route, inclusive = false)
                },
                onStats = {
                    vm.quit()
                    navController.popBackStack(Screen.Menu.route, inclusive = false)
                    navController.navigate(Screen.Stats.route) { launchSingleTop = true }
                },
            )
        }
        composable(Screen.Stats.route) { StatsScreen(vm = vm, onBack = goBack) }
        composable(Screen.Settings.route) { SettingsScreen(vm = vm, onBack = goBack) }
        composable(Screen.Help.route) { HelpScreen(vm = vm, onBack = goBack) }
    }
}

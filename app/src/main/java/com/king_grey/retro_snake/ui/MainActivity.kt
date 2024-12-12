package com.king_grey.retro_snake.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.king_grey.retro_snake.data.Game
import com.king_grey.retro_snake.ui.navigation.SetUpNavigation
import com.king_grey.retro_snake.ui.theme.SnakeGameTheme

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val game = Game(lifecycleScope)

        enableEdgeToEdge()
        installSplashScreen()
        setContent {
            navController = rememberNavController()

            SnakeGameTheme {
                SetUpNavigation(navController = navController, game = game)
            }
        }
    }
}




package com.king_grey.retro_snake.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.king_grey.retro_snake.sound.Sfx
import com.king_grey.retro_snake.sound.Tone
import com.king_grey.retro_snake.ui.navigation.SetUpNavigation
import com.king_grey.retro_snake.ui.theme.LocalClick
import com.king_grey.retro_snake.ui.theme.LocalSfx
import com.king_grey.retro_snake.ui.theme.SnakeGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            val vm: GameViewModel = viewModel()
            val navController = rememberNavController()
            val sfx = remember { Sfx() }
            DisposableEffect(sfx) { onDispose { sfx.release() } }
            val sound = vm.settings.sound
            val click: () -> Unit = remember(sound) { { if (sound) sfx.play(Tone.CLICK) } }

            SnakeGameTheme(vm.settings) {
                CompositionLocalProvider(LocalSfx provides sfx, LocalClick provides click) {
                    SetUpNavigation(navController = navController, vm = vm)
                }
            }
        }
    }
}

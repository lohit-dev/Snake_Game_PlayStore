package com.king_grey.retro_snake.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.king_grey.retro_snake.data.Game
import com.king_grey.retro_snake.ui.components.SnakeGameUi
import com.king_grey.retro_snake.ui.theme.DarkGreen

@Composable
fun GameScreen(game: Game) {
    Surface(modifier = Modifier.fillMaxSize(), color = DarkGreen) {
        SnakeGameUi(game)
    }
}

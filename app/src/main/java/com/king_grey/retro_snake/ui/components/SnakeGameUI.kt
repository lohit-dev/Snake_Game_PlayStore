package com.king_grey.retro_snake.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.king_grey.retro_snake.data.Game


@Composable
fun SnakeGameUi(game: Game) {
    val state = game.state.collectAsState(initial = null)

    Column(
        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        state.value?.let { snake ->
            if (!state.value?.isGameOver!!) {
                Text(
                    text = "Score: ${state.value?.snakeLength?.minus(3)}",
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 4.dp, top = 12.dp)
                )
            }

            if (snake.isGameOver) {
                GameOverScreen(onRestart = { game.resetGame() })
            } else {
                Board(snake)
                Buttons { game.move = it }
            }
        }
    }
}


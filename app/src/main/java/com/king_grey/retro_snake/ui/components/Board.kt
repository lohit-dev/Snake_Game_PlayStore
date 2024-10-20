package com.king_grey.retro_snake.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.king_grey.retro_snake.data.Game
import com.king_grey.retro_snake.data.SnakeState
import com.king_grey.retro_snake.ui.theme.GameBoyGreen
import com.king_grey.retro_snake.ui.theme.LightGreen


@Composable
fun Board(state: SnakeState) {
    BoxWithConstraints(modifier = Modifier.padding(20.dp)) {
        val tileSize = maxWidth / Game.BOARD_WIDTH

        Box(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .size(maxWidth * 1.3F)
                .border(3.dp, Color.DarkGray, RoundedCornerShape(4.dp))
        )

        Box(
            Modifier
                .offset(
                    x = tileSize * state.food.first, y = tileSize * state.food.second
                )
                .size(tileSize)
                .background(GameBoyGreen, CircleShape)
        )

        state.snake.forEach {
            Box(
                modifier = Modifier
                    .offset(x = tileSize * it.first, y = tileSize * it.second)
                    .size(tileSize)
                    .background(LightGreen, RoundedCornerShape(12.dp))
            )
        }
    }
}
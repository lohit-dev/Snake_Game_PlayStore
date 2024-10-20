package com.king_grey.retro_snake.data

data class SnakeState(
    val food: Pair<Int, Int>,
    val snake: List<Pair<Int, Int>>,
    val snakeLength: Int,
    val isGameOver: Boolean = false
)

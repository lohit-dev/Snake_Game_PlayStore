package com.king_grey.retro_snake.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random


class Game(private val scope: CoroutineScope) {
    private val mutex = Mutex()

    private val mutableState =
        MutableStateFlow(SnakeState(food = Pair(5, 5), snake = listOf(Pair(7, 7)), snakeLength = 3))
    val state = mutableState

    var move = Pair(1, 0)
        set(value) {
            scope.launch { mutex.withLock { field = value } }
        }

    private var gameJob: kotlinx.coroutines.Job? = null

    init {
        startGame()
    }

    private fun startGame() {
        gameJob?.cancel()

        gameJob = scope.launch {
            var snakeLength = 3

            while (true) {
                delay(150)

                mutableState.update { currentState ->
                    val newPosition = currentState.snake.first().let { poz ->
                        mutex.withLock {
                            Pair(
                                (poz.first + move.first + BOARD_WIDTH) % BOARD_WIDTH,   // Wrap horizontally
                                (poz.second + move.second + BOARD_HEIGHT) % BOARD_HEIGHT // Wrap vertically
                            )
                        }
                    }


                    // Check for collision with self
                    if (currentState.snake.contains(newPosition)) {
                        return@update currentState.copy(isGameOver = true)
                    }

                    // Check if snake eats food
                    val newFood = if (newPosition == currentState.food) {
                        snakeLength++
                        Pair(Random.nextInt(BOARD_WIDTH), Random.nextInt(BOARD_HEIGHT))
                    } else {
                        currentState.food
                    }

                    currentState.copy(
                        food = newFood,
                        snake = listOf(newPosition) + currentState.snake.take(snakeLength - 1),
                        snakeLength = snakeLength
                    )
                }
            }
        }
    }

    fun resetGame() {
        mutableState.update {
            SnakeState(
                food = Pair(5, 5), snake = listOf(Pair(7, 7)), snakeLength = 3, isGameOver = false
            )
        }
        startGame()
    }

    companion object {
        const val BOARD_WIDTH = 22
        const val BOARD_HEIGHT = 28
    }
}
package com.king_grey.retro_snake.game

import kotlin.random.Random

const val BOARD_W = 22
const val BOARD_H = 28

data class Point(val x: Int, val y: Int)

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

    val opposite: Direction
        get() = when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }
}

enum class Difficulty(val label: String, val baseDelayMs: Long, val stepMs: Long, val minDelayMs: Long) {
    EASY("EASY", 190, 12, 100),
    NORMAL("NORMAL", 150, 10, 80),
    HARD("HARD", 110, 8, 60);

    /** Speed level shown in the HUD: starts at 1, +1 for every 10 apples. */
    fun levelFor(apples: Int): Int = 1 + apples / 10

    fun delayFor(apples: Int): Long =
        maxOf(minDelayMs, baseDelayMs - (levelFor(apples) - 1) * stepMs)
}

enum class GameStatus { READY, RUNNING, PAUSED, OVER }

data class GameState(
    val snake: List<Point>, // head first
    val direction: Direction,
    val food: Point?,
    val apples: Int,
    val status: GameStatus,
    val crashed: Boolean = false,
    val cleared: Boolean = false,
) {
    val head: Point get() = snake.first()
    val length: Int get() = snake.size
}

class StepResult(val state: GameState, val ate: Boolean)

object SnakeEngine {
    fun newGame(random: Random = Random.Default): GameState {
        val snake = listOf(Point(7, 14), Point(6, 14), Point(5, 14))
        return GameState(
            snake = snake,
            direction = Direction.RIGHT,
            food = placeFood(snake, random),
            apples = 0,
            status = GameStatus.READY,
        )
    }

    fun placeFood(snake: List<Point>, random: Random): Point? {
        val taken = snake.toHashSet()
        val free = ArrayList<Point>(BOARD_W * BOARD_H - taken.size)
        for (y in 0 until BOARD_H) for (x in 0 until BOARD_W) {
            val p = Point(x, y)
            if (p !in taken) free.add(p)
        }
        return if (free.isEmpty()) null else free[random.nextInt(free.size)]
    }

    /**
     * Advances one tick in [dir]. A reversal of the current direction is ignored.
     * With [walls] off the board wraps; with walls on, leaving the board ends the game.
     */
    fun step(state: GameState, dir: Direction, walls: Boolean, random: Random = Random.Default): StepResult {
        if (state.status != GameStatus.RUNNING) return StepResult(state, false)
        val d = if (dir == state.direction.opposite) state.direction else dir
        val h = state.head
        var nx = h.x + d.dx
        var ny = h.y + d.dy
        if (walls) {
            if (nx !in 0 until BOARD_W || ny !in 0 until BOARD_H) {
                return StepResult(state.copy(direction = d, status = GameStatus.OVER, crashed = true), false)
            }
        } else {
            nx = Math.floorMod(nx, BOARD_W)
            ny = Math.floorMod(ny, BOARD_H)
        }
        val next = Point(nx, ny)
        val eating = next == state.food
        // When not eating the tail moves away this tick, so its cell is safe to enter.
        val body = if (eating) state.snake else state.snake.dropLast(1)
        if (next in body) {
            return StepResult(state.copy(direction = d, status = GameStatus.OVER, crashed = true), false)
        }
        val newSnake = ArrayList<Point>(state.snake.size + 1)
        newSnake.add(next)
        newSnake.addAll(body)
        if (!eating) {
            return StepResult(state.copy(snake = newSnake, direction = d), false)
        }
        val food = placeFood(newSnake, random)
        return StepResult(
            state.copy(
                snake = newSnake,
                direction = d,
                food = food,
                apples = state.apples + 1,
                status = if (food == null) GameStatus.OVER else GameStatus.RUNNING,
                cleared = food == null,
            ),
            true,
        )
    }
}

/** Buffers up to two queued turns so quick swipes/taps are not dropped between ticks. */
class InputBuffer(private val capacity: Int = 2) {
    private val queue = ArrayDeque<Direction>()

    fun offer(dir: Direction, current: Direction): Boolean {
        val last = queue.lastOrNull() ?: current
        if (dir == last || dir == last.opposite) return false
        if (queue.size >= capacity) return false
        queue.addLast(dir)
        return true
    }

    fun next(current: Direction): Direction = queue.removeFirstOrNull() ?: current

    fun clear() = queue.clear()
}

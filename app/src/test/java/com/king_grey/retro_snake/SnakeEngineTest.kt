package com.king_grey.retro_snake

import com.king_grey.retro_snake.game.*
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class SnakeEngineTest {
    private fun running(snake: List<Point>, dir: Direction, food: Point?) =
        GameState(snake, dir, food, 0, GameStatus.RUNNING)

    @Test fun newGameStartsReadyWithThreeSegments() {
        val g = SnakeEngine.newGame(Random(1))
        assertEquals(3, g.length)
        assertEquals(GameStatus.READY, g.status)
        assertFalse(g.food in g.snake)
    }

    @Test fun movesOneCell() {
        val s = running(listOf(Point(5, 5), Point(4, 5), Point(3, 5)), Direction.RIGHT, Point(0, 0))
        val r = SnakeEngine.step(s, Direction.RIGHT, walls = true)
        assertEquals(Point(6, 5), r.state.head)
        assertEquals(3, r.state.length)
    }

    @Test fun reversalIsIgnored() {
        val s = running(listOf(Point(5, 5), Point(4, 5), Point(3, 5)), Direction.RIGHT, Point(0, 0))
        val r = SnakeEngine.step(s, Direction.LEFT, walls = true)
        assertEquals(Point(6, 5), r.state.head)
        assertEquals(GameStatus.RUNNING, r.state.status)
    }

    @Test fun eatingGrowsAndScores() {
        val s = running(listOf(Point(5, 5), Point(4, 5), Point(3, 5)), Direction.RIGHT, Point(6, 5))
        val r = SnakeEngine.step(s, Direction.RIGHT, walls = true, random = Random(2))
        assertTrue(r.ate)
        assertEquals(4, r.state.length)
        assertEquals(1, r.state.apples)
        assertFalse(r.state.food in r.state.snake)
    }

    @Test fun wallsOnEndsGame() {
        val s = running(listOf(Point(BOARD_W - 1, 5), Point(BOARD_W - 2, 5), Point(BOARD_W - 3, 5)), Direction.RIGHT, Point(0, 0))
        val r = SnakeEngine.step(s, Direction.RIGHT, walls = true)
        assertEquals(GameStatus.OVER, r.state.status)
        assertTrue(r.state.crashed)
    }

    @Test fun wallsOffWraps() {
        val s = running(listOf(Point(BOARD_W - 1, 5), Point(BOARD_W - 2, 5), Point(BOARD_W - 3, 5)), Direction.RIGHT, Point(0, 0).let { Point(10, 10) })
        val r = SnakeEngine.step(s, Direction.RIGHT, walls = false)
        assertEquals(Point(0, 5), r.state.head)
        val up = running(listOf(Point(3, 0), Point(3, 1), Point(3, 2)), Direction.UP, Point(10, 10))
        assertEquals(Point(3, BOARD_H - 1), SnakeEngine.step(up, Direction.UP, walls = false).state.head)
    }

    @Test fun selfCollisionEndsGame() {
        // head at (5,5) heading UP into its own body at (5,4)
        val snake = listOf(Point(5, 5), Point(6, 5), Point(6, 4), Point(5, 4), Point(4, 4), Point(4, 5))
        val s = running(snake, Direction.LEFT, Point(0, 0))
        val r = SnakeEngine.step(s, Direction.UP, walls = false)
        assertEquals(GameStatus.OVER, r.state.status)
    }

    @Test fun canEnterCellTailIsLeaving() {
        // 2x2 loop: head chases its tail and must not die
        val snake = listOf(Point(5, 5), Point(5, 6), Point(6, 6), Point(6, 5))
        val s = running(snake, Direction.UP, Point(0, 0)).copy(direction = Direction.LEFT)
        // head (5,5) moving RIGHT is blocked by reversal rule, so move DOWN? that is body. Use UP->RIGHT turn:
        val r = SnakeEngine.step(s.copy(direction = Direction.UP), Direction.RIGHT, walls = false)
        assertEquals(Point(6, 5), r.state.head)
        assertEquals(GameStatus.RUNNING, r.state.status)
    }

    @Test fun foodNeverSpawnsOnSnake() {
        val snake = (0 until 200).map { Point(it % BOARD_W, it / BOARD_W) }
        repeat(50) { i ->
            val f = SnakeEngine.placeFood(snake, Random(i))
            assertNotNull(f)
            assertFalse(f in snake)
        }
    }

    @Test fun fullBoardIsAWin() {
        val all = ArrayList<Point>()
        // serpentine path covering every cell except (BOARD_W-1, BOARD_H-1)
        for (y in 0 until BOARD_H) {
            val xs = if (y % 2 == 0) (0 until BOARD_W) else (BOARD_W - 1 downTo 0)
            for (x in xs) all.add(Point(x, y))
        }
        val last = all.removeAt(all.size - 1) // the food cell
        val snake = all.reversed() // head first: ends adjacent to `last`
        val dir = if ((BOARD_H - 1) % 2 == 0) Direction.RIGHT else Direction.LEFT
        val s = running(snake, dir, last)
        val r = SnakeEngine.step(s, dir, walls = true)
        assertTrue(r.state.cleared)
        assertEquals(GameStatus.OVER, r.state.status)
    }

    @Test fun difficultyGetsFasterAndHasFloor() {
        for (d in Difficulty.values()) {
            assertTrue(d.delayFor(0) > d.delayFor(30))
            assertEquals(d.minDelayMs, d.delayFor(10_000))
        }
        assertEquals(1, Difficulty.NORMAL.levelFor(9))
        assertEquals(2, Difficulty.NORMAL.levelFor(10))
        assertTrue(Difficulty.HARD.delayFor(0) < Difficulty.EASY.delayFor(0))
    }
}

package com.king_grey.retro_snake

import com.king_grey.retro_snake.game.Direction.*
import com.king_grey.retro_snake.game.InputBuffer
import org.junit.Assert.*
import org.junit.Test

class InputBufferTest {
    @Test fun rejectsSameAndOpposite() {
        val b = InputBuffer()
        assertFalse(b.offer(RIGHT, RIGHT))
        assertFalse(b.offer(LEFT, RIGHT))
        assertTrue(b.offer(UP, RIGHT))
    }

    @Test fun queuesTwoTurnsInOrder() {
        val b = InputBuffer()
        assertTrue(b.offer(UP, RIGHT))
        assertTrue(b.offer(LEFT, RIGHT))
        assertFalse(b.offer(DOWN, RIGHT)) // full
        assertEquals(UP, b.next(RIGHT))
        assertEquals(LEFT, b.next(UP))
        assertEquals(LEFT, b.next(LEFT)) // empty -> keep current
    }

    @Test fun oppositeOfQueuedTurnIsRejected() {
        val b = InputBuffer()
        b.offer(UP, RIGHT)
        assertFalse(b.offer(DOWN, RIGHT))
    }
}

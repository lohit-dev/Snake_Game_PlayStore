package com.king_grey.retro_snake

import com.king_grey.retro_snake.data.*
import com.king_grey.retro_snake.game.Difficulty
import org.junit.Assert.*
import org.junit.Test

class StatsTest {
    private val day = 86_400_000L
    private val now = 1_760_000_000_000L

    private fun run(score: Int, ago: Long, d: Difficulty = Difficulty.NORMAL) = RunRecord(score, d, 60_000, now - ago, 2)

    @Test fun codecRoundTrips() {
        val r = RunRecord(54, Difficulty.HARD, 302_000, 12345L, 6)
        assertEquals(r, RunCodec.decode(RunCodec.encode(r)))
    }

    @Test fun codecSkipsGarbage() {
        val text = "1,EASY,10,5,1\nnope\n2,BOGUS,1,1,1\n\n3,HARD,1,1,1"
        assertEquals(listOf(1, 3), RunCodec.decodeAll(text).map { it.score })
    }

    @Test fun summaryTotalsAndAverage() {
        val s = StatsCalculator.summarize(listOf(run(10, day), run(20, 0)), 4, 40, mapOf(Difficulty.NORMAL to 20), now)
        assertEquals(10.0, s.avgScore, 0.001)
        assertEquals(20, s.bestOverall!!.score)
        assertEquals(20, s.bestByDifficulty[Difficulty.NORMAL])
        assertEquals(0, s.bestByDifficulty[Difficulty.EASY])
        assertEquals(7, s.last7.size)
        assertTrue(s.last7.last().isToday)
    }

    @Test fun emptyHistoryIsSafe() {
        val s = StatsCalculator.summarize(emptyList(), 0, 0, emptyMap(), now)
        assertEquals(0.0, s.avgScore, 0.0)
        assertNull(s.bestOverall)
        assertTrue(s.recent.isEmpty())
    }

    @Test fun last7TakesBestPerDay() {
        val s = StatsCalculator.summarize(listOf(run(5, 0), run(9, 1000)), 2, 14, emptyMap(), now)
        assertEquals(9, s.last7.last().best)
        assertEquals(0, s.last7.first().best)
    }

    @Test fun recentIsNewestFirstMaxThree() {
        val runs = (1..5).map { run(it, (6 - it) * 1000L) }
        assertEquals(listOf(5, 4, 3), StatsCalculator.summarize(runs, 5, 15, emptyMap(), now).recent.map { it.score })
    }

    @Test fun formatsDuration() {
        assertEquals("5:12", StatsCalculator.formatDuration(312_000))
        assertEquals("0:07", StatsCalculator.formatDuration(7_400))
    }

    @Test fun relativeDays() {
        assertEquals("Today", StatsCalculator.relativeDay(now - 1000, now))
        assertEquals("Yest", StatsCalculator.relativeDay(now - day, now))
    }
}

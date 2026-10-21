package com.king_grey.retro_snake.data

import com.king_grey.retro_snake.game.Difficulty
import java.util.Calendar

data class RunRecord(
    val score: Int,
    val difficulty: Difficulty,
    val durationMs: Long,
    val endedAt: Long,
    val topSpeed: Int,
)

object RunCodec {
    fun encode(r: RunRecord) = "${r.score},${r.difficulty.name},${r.durationMs},${r.endedAt},${r.topSpeed}"

    fun decode(line: String): RunRecord? {
        val p = line.trim().split(',')
        if (p.size < 5) return null
        return try {
            RunRecord(
                score = p[0].toInt(),
                difficulty = Difficulty.valueOf(p[1]),
                durationMs = p[2].toLong(),
                endedAt = p[3].toLong(),
                topSpeed = p[4].toInt(),
            )
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun decodeAll(text: String): List<RunRecord> = text.lineSequence().mapNotNull { decode(it) }.toList()
    fun encodeAll(runs: List<RunRecord>): String = runs.joinToString("\n") { encode(it) }
}

data class DayBest(val label: Char, val name: String, val best: Int, val isToday: Boolean)

data class Summary(
    val totalGames: Int,
    val totalApples: Int,
    val avgScore: Double,
    val bestOverall: RunRecord?,
    val bestByDifficulty: Map<Difficulty, Int>,
    val last7: List<DayBest>,
    val recent: List<RunRecord>,
)

object StatsCalculator {
    private val DAY_LETTERS = charArrayOf('S', 'M', 'T', 'W', 'T', 'F', 'S') // Calendar.SUNDAY == 1
    private val DAY_NAMES = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    private fun startOfDay(ms: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = ms
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    /**
     * [runs] is oldest-first. Totals come from the persisted counters so they survive
     * history trimming; everything else is derived from the stored runs.
     */
    fun summarize(runs: List<RunRecord>, totalGames: Int, totalApples: Int, bests: Map<Difficulty, Int>, now: Long): Summary {
        val today = startOfDay(now)
        val days = (6 downTo 0).map { back ->
            val c = Calendar.getInstance()
            c.timeInMillis = today
            c.add(Calendar.DAY_OF_YEAR, -back)
            c
        }
        val last7 = days.mapIndexed { i, c ->
            val s = c.timeInMillis
            val e = Calendar.getInstance().apply { timeInMillis = s; add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            val best = runs.filter { it.endedAt in s until e }.maxOfOrNull { it.score } ?: 0
            DayBest(DAY_LETTERS[c.get(Calendar.DAY_OF_WEEK) - 1], DAY_NAMES[c.get(Calendar.DAY_OF_WEEK) - 1], best, i == 6)
        }
        return Summary(
            totalGames = totalGames,
            totalApples = totalApples,
            avgScore = if (totalGames == 0) 0.0 else totalApples.toDouble() / totalGames,
            bestOverall = runs.maxWithOrNull(compareBy<RunRecord> { it.score }.thenBy { it.endedAt }),
            bestByDifficulty = Difficulty.values().associateWith { bests[it] ?: 0 },
            last7 = last7,
            recent = runs.takeLast(3).reversed(),
        )
    }

    fun formatDuration(ms: Long): String {
        val s = ms / 1000
        return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
    }

    fun relativeDay(endedAt: Long, now: Long): String {
        val diff = ((startOfDay(now) - startOfDay(endedAt)) / 86_400_000L + 0.5).toInt()
        if (diff <= 0) return "Today"
        if (diff == 1) return "Yest"
        val c = Calendar.getInstance().apply { timeInMillis = endedAt }
        return arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")[c.get(Calendar.DAY_OF_WEEK) - 1]
    }
}

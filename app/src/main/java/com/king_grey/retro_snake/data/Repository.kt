package com.king_grey.retro_snake.data

import android.content.Context
import android.content.SharedPreferences
import com.king_grey.retro_snake.game.Difficulty

/** Everything persisted lives in one SharedPreferences file. */
class Repository(context: Context) {
    private val p: SharedPreferences =
        context.applicationContext.getSharedPreferences("retro_snake_prefs", Context.MODE_PRIVATE)

    private inline fun <reified E : Enum<E>> enumOf(key: String, default: E): E =
        try { enumValueOf<E>(p.getString(key, default.name) ?: default.name) } catch (e: IllegalArgumentException) { default }

    fun loadSettings(): Settings {
        val d = Settings()
        return Settings(
            difficulty = enumOf("difficulty", d.difficulty),
            controls = enumOf("controls", d.controls),
            sound = p.getBoolean("sound", d.sound),
            haptics = p.getBoolean("haptics", d.haptics),
            grid = p.getBoolean("grid", d.grid),
            crt = p.getBoolean("crt", d.crt),
            walls = p.getBoolean("walls", d.walls),
            theme = enumOf("theme", d.theme),
        )
    }

    fun saveSettings(s: Settings) {
        p.edit()
            .putString("difficulty", s.difficulty.name)
            .putString("controls", s.controls.name)
            .putBoolean("sound", s.sound)
            .putBoolean("haptics", s.haptics)
            .putBoolean("grid", s.grid)
            .putBoolean("crt", s.crt)
            .putBoolean("walls", s.walls)
            .putString("theme", s.theme.name)
            .apply()
    }

    /** Best score for a difficulty *and* wall mode are tracked together per difficulty. */
    fun best(d: Difficulty): Int = p.getInt("best_${d.name}", 0)

    fun runs(): List<RunRecord> = RunCodec.decodeAll(p.getString("runs", "") ?: "")
    fun totalGames(): Int = p.getInt("total_games", 0)
    fun totalApples(): Int = p.getInt("total_apples", 0)

    fun bests(): Map<Difficulty, Int> = Difficulty.values().associateWith { best(it) }

    /** Records a finished run. Returns true if it set a new best for its difficulty. */
    fun recordRun(r: RunRecord): Boolean {
        val isBest = r.score > best(r.difficulty)
        val history = (runs() + r).takeLast(MAX_RUNS)
        p.edit()
            .putString("runs", RunCodec.encodeAll(history))
            .putInt("total_games", totalGames() + 1)
            .putInt("total_apples", totalApples() + r.score)
            .apply {
                if (isBest) putInt("best_${r.difficulty.name}", r.score)
            }
            .apply()
        return isBest
    }

    fun resetStats() {
        val e = p.edit().remove("runs").remove("total_games").remove("total_apples")
        Difficulty.values().forEach { e.remove("best_${it.name}") }
        e.apply()
    }

    companion object { const val MAX_RUNS = 150 }
}

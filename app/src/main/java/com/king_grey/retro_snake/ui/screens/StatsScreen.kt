package com.king_grey.retro_snake.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.king_grey.retro_snake.data.DayBest
import com.king_grey.retro_snake.data.StatsCalculator
import com.king_grey.retro_snake.data.Summary
import com.king_grey.retro_snake.game.Difficulty
import com.king_grey.retro_snake.ui.GameViewModel
import com.king_grey.retro_snake.ui.components.ExtrudedText
import com.king_grey.retro_snake.ui.components.MonoText
import com.king_grey.retro_snake.ui.components.PixelPanel
import com.king_grey.retro_snake.ui.components.PixelText
import com.king_grey.retro_snake.ui.components.RetroScreen
import com.king_grey.retro_snake.ui.components.SegBar
import com.king_grey.retro_snake.ui.theme.LocalRetro
import kotlin.math.roundToInt

private fun Difficulty.title() = label.lowercase().replaceFirstChar { it.uppercase() }

@Composable
fun StatsScreen(vm: GameViewModel, onBack: () -> Unit) {
    val c = LocalRetro.current
    val summary = remember(vm.statsVersion) { vm.summary() }
    val now = remember(vm.statsVersion) { System.currentTimeMillis() }

    RetroScreen {
        Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 20.dp)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScreenTitle("STATS")
                    MonoText(
                        if (summary.totalGames == 0) "No games yet. Go set a score!" else "${summary.totalGames} games played so far",
                        13, c.muted,
                    )
                }
                BestPanel(summary, now)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tile("GAMES", "%,d".format(summary.totalGames), Modifier.weight(1f))
                    Tile("APPLES", "%,d".format(summary.totalApples), Modifier.weight(1f))
                    Tile("AVG SCORE", "%.1f".format(summary.avgScore), Modifier.weight(1f))
                }
                WeekChart(summary.last7)
                RecentRuns(summary, now)
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(14.dp))
            BackButton(onBack)
        }
    }
}

@Composable
private fun Tile(label: String, value: String, modifier: Modifier) {
    val c = LocalRetro.current
    PixelPanel(modifier) {
        Column(Modifier.padding(10.dp)) {
            MonoText(label, 12, c.muted, letterSpacing = 0.96f, lineHeight = 16)
            Spacer(Modifier.height(8.dp))
            PixelText(value, 15, c.text)
        }
    }
}

@Composable
private fun BestPanel(s: Summary, now: Long) {
    val c = LocalRetro.current
    val top = s.bestByDifficulty.values.maxOrNull() ?: 0
    val topDiff = s.bestByDifficulty.entries.firstOrNull { it.value == top && top > 0 }?.key
    val run = s.bestOverall?.takeIf { it.score == top && top > 0 }
    val sub = when {
        topDiff == null -> "Play a game to set one"
        run != null -> "${topDiff.title()} · ${StatsCalculator.relativeDay(run.endedAt, now)}"
        else -> topDiff.title()
    }
    PixelPanel(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) {
                MonoText("ALL-TIME BEST", 12, c.muted, letterSpacing = 1.7f)
                Spacer(Modifier.height(10.dp))
                ExtrudedText(if (top == 0) "--" else top.toString(), 48, c.accent, c.accentDark, null, step = 3.dp)
                Spacer(Modifier.height(10.dp))
                MonoText(sub, 13, c.muted)
            }
            Column(Modifier.width(148.dp), verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
                for (d in Difficulty.values()) {
                    val v = s.bestByDifficulty[d] ?: 0
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            MonoText(d.label, 12, c.muted, letterSpacing = 1.2f, lineHeight = 16)
                            PixelText(v.toString(), 12, c.text)
                        }
                        Box(Modifier.fillMaxWidth().height(8.dp).background(c.deep)) {
                            SegBar(if (top == 0) 0f else v.toFloat() / top, c.data, Modifier.fillMaxWidth().height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekChart(days: List<DayBest>) {
    val c = LocalRetro.current
    val label = "Best score per day this week: " + days.joinToString(", ") { "${it.name} ${it.best}" }
    val denom = maxOf(60, days.maxOfOrNull { it.best } ?: 0)
    PixelPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth().height(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                MonoText("LAST 7 DAYS", 12, c.muted, letterSpacing = 1.7f, lineHeight = 16)
                MonoText("Best score per day", 12, c.muted, lineHeight = 16)
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp).semantics { contentDescription = label },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (d in days) {
                    val col = if (d.isToday) c.accent else c.data
                    val barH = if (d.best == 0) 4 else maxOf(8, (d.best.toFloat() / denom * 64 / 8).roundToInt() * 8)
                    Column(
                        Modifier.weight(1f).height(110.dp).clearAndSetSemantics { },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        MonoText(d.best.toString(), 12, if (d.isToday) c.text else c.muted, lineHeight = 16)
                        Spacer(Modifier.height(4.dp))
                        if (d.best == 0) {
                            Box(Modifier.width(24.dp).height(4.dp).background(c.border))
                        } else {
                            SegBar(1f, col, Modifier.width(24.dp).height(barH.dp), vertical = true)
                        }
                        Spacer(Modifier.height(8.dp))
                        MonoText(d.label.toString(), 12, if (d.isToday) c.text else c.muted, weight = if (d.isToday) FontWeight.SemiBold else FontWeight.Normal, lineHeight = 16)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentRuns(s: Summary, now: Long) {
    val c = LocalRetro.current
    PixelPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            MonoText("RECENT RUNS", 12, c.muted, letterSpacing = 1.7f, lineHeight = 16)
            Spacer(Modifier.height(8.dp))
            if (s.recent.isEmpty()) {
                MonoText("Nothing here yet. Your last three runs show up after you play.", 13, c.muted, Modifier.padding(vertical = 8.dp))
            }
            for ((score, difficulty, durationMs, endedAt) in s.recent) {
                val isBest = score > 0 && score == (s.bestByDifficulty[difficulty] ?: 0)
                Box(Modifier.fillMaxWidth().height(2.dp).background(c.border))
                Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PixelText(score.toString(), 16, c.text, Modifier.width(52.dp))
                    Column(Modifier.weight(1f)) {
                        MonoText(difficulty.title(), 14, c.text, weight = FontWeight.Medium, lineHeight = 18)
                        MonoText(StatsCalculator.formatDuration(durationMs), 12, c.muted, lineHeight = 16)
                    }
                    if (isBest) {
                        Box(Modifier.background(c.accent).padding(horizontal = 6.dp, vertical = 3.dp)) {
                            PixelText("BEST", 9, c.onAccent)
                        }
                    }
                    MonoText(StatsCalculator.relativeDay(endedAt, now), 12, c.muted, Modifier.width(40.dp), align = androidx.compose.ui.text.style.TextAlign.End)
                }
            }
        }
    }
}

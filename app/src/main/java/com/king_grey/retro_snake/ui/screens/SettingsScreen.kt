package com.king_grey.retro_snake.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.king_grey.retro_snake.data.ControlMode
import com.king_grey.retro_snake.data.Settings
import com.king_grey.retro_snake.data.ThemeId
import com.king_grey.retro_snake.ui.GameViewModel
import com.king_grey.retro_snake.ui.components.MonoText
import com.king_grey.retro_snake.ui.components.Notch3
import com.king_grey.retro_snake.ui.components.Notch6
import com.king_grey.retro_snake.ui.components.PixelButton
import com.king_grey.retro_snake.ui.components.PixelPanel
import com.king_grey.retro_snake.ui.components.PixelText
import com.king_grey.retro_snake.ui.components.RetroScreen
import com.king_grey.retro_snake.ui.theme.FocusCream
import com.king_grey.retro_snake.ui.theme.LocalClick
import com.king_grey.retro_snake.ui.theme.LocalRetro
import com.king_grey.retro_snake.ui.theme.Palettes
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.selection.toggleable as foundationToggleable

@Composable
fun SettingsScreen(vm: GameViewModel, onBack: () -> Unit) {
    val c = LocalRetro.current
    val s = vm.settings
    var confirm by remember { mutableStateOf(false) }
    val version = appVersion(LocalContext.current)

    BackHandler(enabled = confirm) { confirm = false }

    RetroScreen {
        Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 20.dp)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ScreenTitle("SETTINGS")

                Section("CONTROLS") {
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (m in ControlMode.values()) {
                            val on = s.controls == m
                            Box(Modifier.weight(1f)) {
                                ChoiceChip(m.label, on, "Control style ${m.label}") { vm.update { it.copy(controls = m) } }
                            }
                        }
                    }
                }

                Section("PREFERENCES") {
                    Column(Modifier.padding(top = 8.dp)) {
                        PrefRow("Sound effects", "Chomp and game-over tones", s.sound) { v -> vm.update { it.copy(sound = v) } }
                        PrefRow("Vibration", "Buzz when you eat or crash", s.haptics) { v -> vm.update { it.copy(haptics = v) } }
                        PrefRow("Show grid dots", "Faint dots behind the board", s.grid) { v -> vm.update { it.copy(grid = v) } }
                        PrefRow("CRT scanlines", "Old-TV lines and glow", s.crt) { v -> vm.update { it.copy(crt = v) } }
                        PrefRow("Wall collisions", "Off: edges wrap around", s.walls) { v -> vm.update { it.copy(walls = v) } }
                    }
                }

                Section("COLOUR THEME") {
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (t in ThemeId.values()) {
                            Box(Modifier.weight(1f)) { ThemeChip(t, s.theme == t) { vm.update { it.copy(theme = t) } } }
                        }
                    }
                }

                PixelButton(
                    onClick = { confirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp,
                    border = c.danger,
                    face = c.bg,
                ) { PixelText("RESET ALL STATS", 12, c.danger) }

                MonoText(
                    "Retro Snake" + if (version.isNotEmpty()) " · v$version" else "",
                    12, c.muted, Modifier.fillMaxWidth(), align = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            Spacer(Modifier.height(14.dp))
            BackButton(onBack)
        }

        if (confirm) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xDB08070C))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                PixelPanel(Modifier.fillMaxWidth().semantics { paneTitle = "Reset all stats" }) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        PixelText("RESET STATS?", 16, c.danger)
                        MonoText("This clears your high scores, totals and recent runs. It can't be undone.", 13, c.muted, align = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        PixelButton(onClick = { vm.resetStats(); confirm = false }, modifier = Modifier.fillMaxWidth(), height = 56.dp, border = c.dangerDark, face = c.danger) {
                            PixelText("YES, RESET", 14, c.onAccent)
                        }
                        PixelButton(onClick = { confirm = false }, modifier = Modifier.fillMaxWidth(), height = 56.dp) {
                            PixelText("CANCEL", 14, c.text)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val c = LocalRetro.current
    PixelPanel(Modifier.fillMaxWidth().semantics { contentDescription = title }) {
        Column(Modifier.padding(12.dp)) {
            MonoText(title, 12, c.muted, letterSpacing = 1.7f, lineHeight = 16)
            content()
        }
    }
}

@Composable
private fun ChoiceChip(label: String, on: Boolean, description: String, onClick: () -> Unit) {
    val c = LocalRetro.current
    PixelButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        height = 44.dp,
        border = if (on) c.accentDark else c.border,
        face = if (on) c.accent else c.bg,
        description = if (on) "$description, selected" else description,
    ) { PixelText(label, 11, if (on) c.onAccent else c.text) }
}

@Composable
private fun PrefRow(label: String, hint: String, on: Boolean, onChange: (Boolean) -> Unit) {
    val c = LocalRetro.current
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            MonoText(label, 15, c.text, weight = FontWeight.Medium, lineHeight = 20)
            MonoText(hint, 12, c.muted, lineHeight = 16)
        }
        PixelSwitch(on, label, onChange)
    }
}

@Composable
private fun PixelSwitch(on: Boolean, label: String, onChange: (Boolean) -> Unit) {
    val c = LocalRetro.current
    val click = LocalClick.current
    Box(
        Modifier
            .size(72.dp, 44.dp)
            .clip(Notch6)
            .background(if (on) Color(0xFFBFE3FF) else c.border)
            .switchToggle(on, label) { click(); onChange(it) }
            .padding(3.dp)
            .clip(Notch3)
            .background(if (on) c.data else c.deep)
            .padding(horizontal = 7.dp),
    ) {
        val fg = if (on) c.onAccent else c.muted
        val knob = if (on) c.onAccent else Color(0xFF8C879E)
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            if (on) {
                PixelText("ON", 9, fg)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(16.dp).background(knob))
            } else {
                Box(Modifier.size(16.dp).background(knob))
                Spacer(Modifier.weight(1f))
                PixelText("OFF", 9, fg)
            }
        }
    }
}

private fun Modifier.switchToggle(on: Boolean, label: String, onChange: (Boolean) -> Unit): Modifier =
    this.semantics { contentDescription = label }
        .foundationToggleable(value = on, role = Role.Switch, onValueChange = onChange)

@Composable
private fun ThemeChip(t: ThemeId, on: Boolean, onClick: () -> Unit) {
    val c = LocalRetro.current
    val p = Palettes.of(t)
    PixelButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        height = 76.dp,
        border = if (on) FocusCream else c.border,
        face = c.bg,
        description = if (on) "${t.label} theme, selected" else "${t.label} theme",
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
            Row(
                Modifier.fillMaxWidth().height(36.dp).background(p.bg),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(3) { Box(Modifier.size(8.dp).background(p.snakeBody)) }
                Box(Modifier.padding(start = 4.dp).size(8.dp).background(p.food))
            }
            MonoText(t.label, 12, c.text, weight = FontWeight.Medium, lineHeight = 16)
        }
    }
}

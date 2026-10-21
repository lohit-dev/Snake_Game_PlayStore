package com.king_grey.retro_snake.ui.theme

import android.provider.Settings as AndroidSettings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.king_grey.retro_snake.data.Settings

val LocalRetro = staticCompositionLocalOf { Palettes.Night }
val LocalSettings = compositionLocalOf { Settings() }
val LocalReduceMotion = staticCompositionLocalOf { false }

@Composable
fun SnakeGameTheme(settings: Settings, content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val reduce = remember(ctx) {
        try {
            AndroidSettings.Global.getFloat(ctx.contentResolver, AndroidSettings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        } catch (e: Exception) {
            false
        }
    }
    CompositionLocalProvider(
        LocalRetro provides Palettes.of(settings.theme),
        LocalSettings provides settings,
        LocalReduceMotion provides reduce,
        content = content,
    )
}

val LocalSfx = staticCompositionLocalOf<com.king_grey.retro_snake.sound.Sfx?> { null }
val LocalClick = staticCompositionLocalOf<() -> Unit> { {} }

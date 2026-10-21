package com.king_grey.retro_snake.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.king_grey.retro_snake.ui.components.ExtrudedText
import com.king_grey.retro_snake.ui.components.PixelButton
import com.king_grey.retro_snake.ui.components.PixelText
import com.king_grey.retro_snake.ui.components.TriangleIcon
import com.king_grey.retro_snake.ui.theme.LocalRetro

/** Screen title with the dashed rule that fills the rest with the row. */
@Composable
fun ScreenTitle(text: String, size: Int = 20, trailing: @Composable () -> Unit = {}) {
    val c = LocalRetro.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        ExtrudedText(text, size, c.text, c.accentDark, null, step = 2.dp)
        Spacer(Modifier.weight(1f).height(4.dp).dashedRule(c.border))
        trailing()
    }
}

private fun Modifier.dashedRule(color: Color): Modifier = this.drawBehind {
    val on = 8.dp.toPx()
    val off = 4.dp.toPx()
    var x = 0f
    while (x < size.width) {
        drawRect(color, Offset(x, 0f), Size(minOf(on, size.width - x), size.height))
        x += on + off
    }
}

/** The BACK button pinned to the bottom of sub-screens. */
@Composable
fun BackButton(onClick: () -> Unit, label: String = "BACK") {
    val c = LocalRetro.current
    PixelButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), height = 56.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TriangleIcon(false, c.accent)
            PixelText(label, 16, c.text)
        }
    }
}

package io.github.raghavg02.fosspulse.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.brutalShadow(
    offsetX: Dp = 4.dp,
    offsetY: Dp = 4.dp,
    color: Color = BrutalColors.Ink,
    cornerRadius: Dp = 0.dp
): Modifier = this.drawBehind {
    val radiusPx = cornerRadius.toPx()
    val offXPx = offsetX.toPx()
    val offsetYPx = offsetY.toPx()

    if (radiusPx > 0f) {
        drawRoundRect(
            color = color,
            topLeft = Offset(offXPx, offsetYPx),
            size = size,
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )
    } else {
        drawRect(
            color = color,
            topLeft = Offset(offXPx, offsetYPx),
            size = size
        )
    }
}

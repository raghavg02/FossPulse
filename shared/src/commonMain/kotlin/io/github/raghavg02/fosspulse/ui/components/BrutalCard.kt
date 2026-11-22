package io.github.raghavg02.fosspulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme
import io.github.raghavg02.fosspulse.ui.theme.brutalShadow

@Composable
fun BrutalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrutalTheme.colors.surface,
    borderColor: Color = BrutalTheme.colors.border,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 4.dp,
    cornerRadius: Dp = 6.dp,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .brutalShadow(
                offsetX = shadowOffset,
                offsetY = shadowOffset,
                color = borderColor,
                cornerRadius = cornerRadius
            )
            .clip(shape)
            .background(backgroundColor, shape)
            .border(borderWidth, borderColor, shape)
            .then(clickModifier)
            .padding(contentPadding)
    ) {
        content()
    }
}

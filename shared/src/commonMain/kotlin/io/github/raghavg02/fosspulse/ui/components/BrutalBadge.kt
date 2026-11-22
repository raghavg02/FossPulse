package io.github.raghavg02.fosspulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme

enum class BrutalBadgeStyle {
    UP_TO_DATE,
    UPDATE_READY,
    PLATFORM,
    NEUTRAL,
    ERROR
}

@Composable
fun BrutalBadge(
    text: String,
    modifier: Modifier = Modifier,
    style: BrutalBadgeStyle = BrutalBadgeStyle.NEUTRAL
) {
    val backgroundColor = when (style) {
        BrutalBadgeStyle.UP_TO_DATE -> BrutalTheme.colors.success
        BrutalBadgeStyle.UPDATE_READY -> BrutalTheme.colors.warning
        BrutalBadgeStyle.PLATFORM -> BrutalTheme.colors.secondary
        BrutalBadgeStyle.NEUTRAL -> BrutalTheme.colors.canvas
        BrutalBadgeStyle.ERROR -> BrutalTheme.colors.error
    }

    val shape = RoundedCornerShape(2.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor, shape)
            .border(1.5.dp, BrutalTheme.colors.border, shape)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = BrutalTheme.typography.MonoSmall,
            color = if (style == BrutalBadgeStyle.ERROR) Color.White else BrutalTheme.colors.ink
        )
    }
}

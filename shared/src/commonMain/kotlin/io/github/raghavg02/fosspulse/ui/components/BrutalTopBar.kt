package io.github.raghavg02.fosspulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme

@Composable
fun BrutalTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: (@Composable () -> Unit)? = null
) {
    val borderColor = BrutalTheme.colors.border

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BrutalTheme.colors.surface)
            .statusBarsPadding()
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = BrutalTheme.typography.H2,
                color = BrutalTheme.colors.ink
            )

            if (actions != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions()
                }
            }
        }
    }
}

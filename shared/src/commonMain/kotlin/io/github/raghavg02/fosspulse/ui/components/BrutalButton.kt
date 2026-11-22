package io.github.raghavg02.fosspulse.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme
import io.github.raghavg02.fosspulse.ui.theme.brutalShadow

enum class BrutalButtonType {
    PRIMARY,
    SECONDARY,
    DANGER
}

@Composable
fun BrutalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: BrutalButtonType = BrutalButtonType.PRIMARY,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentPressed = isPressed && enabled

    val translationOffset by animateDpAsState(
        targetValue = if (currentPressed) 3.dp else 0.dp,
        label = "btnTranslate"
    )
    val shadowOffset by animateDpAsState(
        targetValue = if (currentPressed) 1.dp else 4.dp,
        label = "btnShadow"
    )

    val backgroundColor = when (type) {
        BrutalButtonType.PRIMARY -> BrutalTheme.colors.primary
        BrutalButtonType.SECONDARY -> BrutalTheme.colors.surface
        BrutalButtonType.DANGER -> BrutalTheme.colors.error
    }

    val borderWidth = if (type == BrutalButtonType.PRIMARY) 3.dp else 2.dp
    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .offset(x = translationOffset, y = translationOffset)
            .brutalShadow(
                offsetX = shadowOffset,
                offsetY = shadowOffset,
                color = BrutalTheme.colors.border,
                cornerRadius = 4.dp
            )
            .clip(shape)
            .background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f), shape)
            .border(borderWidth, BrutalTheme.colors.border, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = BrutalTheme.typography.Button,
            color = if (type == BrutalButtonType.DANGER) Color.White else BrutalTheme.colors.ink
        )
    }
}

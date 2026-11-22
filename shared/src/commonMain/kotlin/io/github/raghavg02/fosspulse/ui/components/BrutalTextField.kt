package io.github.raghavg02.fosspulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme
import io.github.raghavg02.fosspulse.ui.theme.brutalShadow

@Composable
fun BrutalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isMonospace: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val shape = RoundedCornerShape(4.dp)
    val textStyle = if (isMonospace) {
        BrutalTheme.typography.MonoBadge.copy(color = BrutalTheme.colors.ink)
    } else {
        BrutalTheme.typography.Body.copy(color = BrutalTheme.colors.ink)
    }

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label.uppercase(),
                style = BrutalTheme.typography.Button,
                color = BrutalTheme.colors.ink,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle,
            cursorBrush = SolidColor(BrutalTheme.colors.ink),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .brutalShadow(
                            offsetX = 3.dp,
                            offsetY = 3.dp,
                            color = BrutalTheme.colors.shadow,
                            cornerRadius = 4.dp
                        )
                        .clip(shape)
                        .background(BrutalTheme.colors.surface, shape)
                        .border(2.dp, if (errorMessage != null) BrutalTheme.colors.error else BrutalTheme.colors.border, shape)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    if (value.isEmpty() && !placeholder.isNullOrBlank()) {
                        Text(
                            text = placeholder,
                            style = textStyle.copy(color = BrutalTheme.colors.muted)
                        )
                    } else {
                        innerTextField()
                    }
                }
            }
        )

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = BrutalTheme.typography.BodySmall.copy(color = BrutalTheme.colors.error),
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
        }
    }
}

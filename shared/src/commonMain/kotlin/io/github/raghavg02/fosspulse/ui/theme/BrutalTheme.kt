package io.github.raghavg02.fosspulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class BrutalColorPalette(
    val canvas: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val ink: Color,
    val border: Color,
    val muted: Color,
    val success: Color,
    val warning: Color,
    val error: Color
)

val LocalBrutalColors = staticCompositionLocalOf {
    BrutalColorPalette(
        canvas = BrutalColors.CanvasLight,
        surface = BrutalColors.SurfaceLight,
        primary = BrutalColors.CyberYellow,
        secondary = BrutalColors.ElectricLavender,
        ink = BrutalColors.Ink,
        border = BrutalColors.Ink,
        muted = BrutalColors.MutedText,
        success = BrutalColors.MintGreen,
        warning = BrutalColors.SignalTangerine,
        error = BrutalColors.RawCrimson
    )
}

@Composable
fun BrutalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        BrutalColorPalette(
            canvas = BrutalColors.CanvasDark,
            surface = BrutalColors.SurfaceDark,
            primary = BrutalColors.CyberYellow,
            secondary = BrutalColors.ElectricLavender,
            ink = BrutalColors.Ink,
            border = BrutalColors.InkDarkBorder,
            muted = BrutalColors.MutedTextDark,
            success = BrutalColors.MintGreen,
            warning = BrutalColors.SignalTangerine,
            error = BrutalColors.RawCrimson
        )
    } else {
        BrutalColorPalette(
            canvas = BrutalColors.CanvasLight,
            surface = BrutalColors.SurfaceLight,
            primary = BrutalColors.CyberYellow,
            secondary = BrutalColors.ElectricLavender,
            ink = BrutalColors.Ink,
            border = BrutalColors.Ink,
            muted = BrutalColors.MutedText,
            success = BrutalColors.MintGreen,
            warning = BrutalColors.SignalTangerine,
            error = BrutalColors.RawCrimson
        )
    }

    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            background = colors.canvas,
            surface = colors.surface,
            onBackground = colors.border,
            onSurface = colors.border
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            background = colors.canvas,
            surface = colors.surface,
            onBackground = colors.ink,
            onSurface = colors.ink
        )
    }

    CompositionLocalProvider(
        LocalBrutalColors provides colors
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}

object BrutalTheme {
    val colors: BrutalColorPalette
        @Composable
        get() = LocalBrutalColors.current

    val typography: BrutalTypography
        get() = BrutalTypography
}

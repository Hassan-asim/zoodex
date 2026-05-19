package com.Sufi.zoodex.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme
import com.Sufi.zoodex.data.GameState

private val AppleColorScheme = darkColorScheme(
    primary = AppleBlue,
    secondary = NeonViolet,
    background = ObsidianBlack,
    surface = GlassSurface,
    onPrimary = ObsidianBlack,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val AppleLightColorScheme = lightColorScheme(
    primary = AppleBlue,
    secondary = NeonViolet,
    background = Color(0xFFF2F4F8),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color(0xFF1A1F2C),
    onBackground = Color(0xFF1A1F2C),
    onSurface = Color(0xFF1A1F2C)
)

@Composable
fun ZoodexTheme(
    darkTheme: Boolean = GameState.isDarkTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AppleColorScheme else AppleLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CyberTypography,
        content = content
    )
}

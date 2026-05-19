package com.Sufi.zoodex.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.Sufi.zoodex.data.GameState

// Apple-style Obsidian design system color tokens
val ObsidianBlack: Color
    get() = if (GameState.isDarkTheme) Color(0xFF0A0A0E) else Color(0xFFF2F4F8)

val GlassSurface: Color
    get() = if (GameState.isDarkTheme) Color(0xFF16171D) else Color(0xFFEBF0FA)

val AppleBlue = Color(0xFF0A84FF)
val AppleGreen = Color(0xFF30D158)
val AppleOrange = Color(0xFFFF9F0A)
val AppleRed = Color(0xFFFF453A)

val TextPrimary: Color
    get() = if (GameState.isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF1C1C1E)

val TextSecondary: Color
    get() = if (GameState.isDarkTheme) Color(0xFF8E8E93) else Color(0xFF55555C)

val TextTertiary: Color
    get() = if (GameState.isDarkTheme) Color(0xFF48484A) else Color(0xFFC7C7CC)

// Premium Unified Cyber Gradient visual identities
val CyberBlueStart = Color(0xFF007AFF) // High-tech cobalt blue
val CyberBlueEnd = Color(0xFF8944FD)   // Hyper-neon violet

val CyberGradient = Brush.horizontalGradient(
    colors = listOf(CyberBlueStart, CyberBlueEnd)
)

val CyberGradientRadial = Brush.radialGradient(
    colors = listOf(CyberBlueStart.copy(0.25f), Color.Transparent)
)

// Legacy compatibility fallbacks mapped to unified palette
val NeonCyan = CyberBlueStart
val NeonViolet = CyberBlueEnd
val NeonRed = CyberBlueStart

val GlassWhite: Color
    get() = if (GameState.isDarkTheme) Color(0x0EFFFFFF) else Color(0x0A000000)

val MidnightSpace: Color
    get() = ObsidianBlack

val DarkSurface: Color
    get() = GlassSurface

val CyberLime = CyberBlueStart
val VoltViolet = CyberBlueEnd

package com.Sufi.zoodex.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.Sufi.zoodex.data.GameState

// Apple-style Obsidian design system color tokens
val ObsidianBlack: Color
    get() = if (GameState.isDarkTheme) Color(0xFF0A0A0E) else Color(0xFFEBEFF5)

val GlassSurface: Color
    get() = if (GameState.isDarkTheme) Color(0xFF16171D) else Color(0xFFFFFFFF)

// Dynamic Color Tokens with rich contrast in Light Mode (No light-on-light)
val AppleBlue: Color
    get() = if (GameState.isDarkTheme) Color(0xFF0A84FF) else Color(0xFF0056D6)

val AppleGreen: Color
    get() = if (GameState.isDarkTheme) Color(0xFF30D158) else Color(0xFF1A7F37)

val AppleOrange: Color
    get() = if (GameState.isDarkTheme) Color(0xFFFF9F0A) else Color(0xFFC25100)

val AppleRed: Color
    get() = if (GameState.isDarkTheme) Color(0xFFFF453A) else Color(0xFFD11A1A)

val TextPrimary: Color
    get() = if (GameState.isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF151821)

val TextSecondary: Color
    get() = if (GameState.isDarkTheme) Color(0xFF8E8E93) else Color(0xFF4A4B54)

val TextTertiary: Color
    get() = if (GameState.isDarkTheme) Color(0xFF48484A) else Color(0xFF757680)

/** Dividers and card hairlines — dark surfaces use white mist; light surfaces use charcoal mist. */
val HairlineDivider: Color
    get() = if (GameState.isDarkTheme) Color.White.copy(0.08f) else Color(0xFF3C3C43).copy(0.18f)

// Premium Unified Cyber Gradient visual identities
val CyberBlueStart: Color
    get() = if (GameState.isDarkTheme) Color(0xFF007AFF) else Color(0xFF0056D6)
val CyberBlueEnd: Color
    get() = if (GameState.isDarkTheme) Color(0xFF8944FD) else Color(0xFF6724D6)

val CyberGradient: Brush
    get() = Brush.horizontalGradient(
        colors = listOf(CyberBlueStart, CyberBlueEnd)
    )

val CyberGradientRadial: Brush
    get() = Brush.radialGradient(
        colors = listOf(CyberBlueStart.copy(0.25f), Color.Transparent)
    )

// Legacy compatibility fallbacks mapped to unified palette
val NeonCyan: Color
    get() = CyberBlueStart
val NeonViolet: Color
    get() = CyberBlueEnd
val NeonRed: Color
    get() = CyberBlueStart

val GlassWhite: Color
    get() = if (GameState.isDarkTheme) Color(0x0EFFFFFF) else Color(0x0A000000)

val MidnightSpace: Color
    get() = ObsidianBlack

val DarkSurface: Color
    get() = GlassSurface

val CyberLime: Color
    get() = CyberBlueStart
val VoltViolet: Color
    get() = CyberBlueEnd

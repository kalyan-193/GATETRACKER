package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = CyanGlow,
    tertiary = NebulaPurple,
    background = DeepSpaceDark,
    surface = Color(0x11FFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = CosmicWhiteText,
    onSurface = CosmicWhiteText,
    surfaceVariant = Color(0x190A0D1A),
    onSurfaceVariant = CyberSlateText
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    secondary = CyanGlow,
    tertiary = NebulaPurple,
    background = Color(0xFFF1F5F9), // Slate 100 for light mode
    surface = Color(0xB2FFFFFF),    // Milky white glass
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A), // Slate 900
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Mode by default for premium Liquid Glass vibe
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

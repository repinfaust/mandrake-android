package com.repinfaust.mandrake.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme using Mandrake brand colors
private val LightColorScheme = lightColorScheme(
    primary = DarkForestGreen,
    onPrimary = Cream,
    primaryContainer = SageGreen,
    onPrimaryContainer = DarkForestGreen,
    secondary = SageGreen,
    onSecondary = DarkForestGreen,
    background = Cream,
    onBackground = DarkForestGreen,
    surface = Cream,
    onSurface = DarkForestGreen,
    surfaceVariant = Color(0xFFE8E1D8), // Slightly darker cream
    onSurfaceVariant = DarkForestGreen
)

// Dark theme using Mandrake brand colors
private val DarkColorScheme = darkColorScheme(
    primary = SageGreen,
    onPrimary = BlackGreen,
    primaryContainer = DarkForestGreen,
    onPrimaryContainer = SageGreen,
    secondary = SageGreen,
    onSecondary = BlackGreen,
    background = BlackGreen,
    onBackground = SageGreen,
    surface = DarkForestGreen,
    onSurface = SageGreen,
    surfaceVariant = Color(0xFF1A2F28), // Slightly lighter black-green
    onSurfaceVariant = SageGreen
)

@Composable
fun UrgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

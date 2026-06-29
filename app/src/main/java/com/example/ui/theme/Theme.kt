package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberTeal,
    onPrimary = Color.Black,
    secondary = CyberGreen,
    onSecondary = Color.Black,
    tertiary = CyberBlue,
    background = CyberDarkBg,
    onBackground = CyberTextPrimary,
    surface = CyberCardBg,
    onSurface = CyberTextPrimary,
    error = CyberRed,
    onError = Color.Black,
    outline = CyberCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for executive cyber aesthetic
    dynamicColor: Boolean = false, // Disable dynamic colors to keep brand colors intact
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}

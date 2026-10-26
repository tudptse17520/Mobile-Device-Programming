package com.example.librarymanagementapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    secondary = BlueSecondary,
    background = Color(0xFFF9FAFB),
    surface = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = BlueSecondary,
    onPrimary = Color.White,
    secondary = BluePrimary,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

@Composable
fun LibraryManagementAppTheme(
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

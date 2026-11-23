package com.example.firebaseauth.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val LightColorScheme = lightColorScheme(
    primary = UthBlue,
    onPrimary = Color.White,
    secondary = UthOrange,
    tertiary = UthGreen,
    background = Color.White,
    surface = Color.White,
    onBackground = UthInk,
    onSurface = UthInk
)

@Composable
fun FirebaseAuthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

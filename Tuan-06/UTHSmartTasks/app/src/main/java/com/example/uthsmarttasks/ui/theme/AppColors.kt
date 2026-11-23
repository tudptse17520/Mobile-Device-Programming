package com.example.uthsmarttasks.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

internal val TodoAccents = mapOf(
    "blue" to Color(0xFF2F5BEA),
    "green" to Color(0xFF0F8A5F),
    "orange" to Color(0xFFC2410C),
    "purple" to Color(0xFF7C3AED)
)

internal fun todoColors(dark: Boolean, accentId: String = "blue"): ColorScheme {
    val accent = TodoAccents[accentId] ?: TodoAccents.getValue("blue")
    return if (dark) {
        darkColorScheme(
            primary = accent,
            background = Color.Black,
            surface = Color(0xFF1C1C1E),
            surfaceVariant = Color(0xFF2C2C2E),
            onBackground = Color.White,
            onSurface = Color(0xFFF2F2F7),
            onSurfaceVariant = Color(0xFFA1A1A8)
        )
    } else {
        lightColorScheme(
            primary = accent,
            background = Color(0xFFF5F5F7),
            surface = Color.White,
            surfaceVariant = Color(0xFFE9E9EE),
            onBackground = Color(0xFF1C1C1E),
            onSurface = Color(0xFF1C1C1E),
            onSurfaceVariant = Color(0xFF6B6B70)
        )
    }
}

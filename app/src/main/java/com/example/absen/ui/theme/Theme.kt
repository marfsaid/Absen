// ======================================================================
// FILE: app/src/main/java/com/example/absen/ui/theme/Theme.kt
// Putih, minimalis, performa aman. (Kalau sudah ada theme, timpa bagian ColorScheme saja)
// ======================================================================
package com.example.absen.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Color(0xFF1F6FEB),
    onPrimary = Color.White,
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFE5E7EB),
    error = Color(0xFFB42318)
)

@Composable
fun AbsenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        typography = Typography,
        content = content
    )
}

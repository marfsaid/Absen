// ======================================================================
// FILE: app/src/main/java/com/example/absen/ui/theme/Type.kt
// ======================================================================
package com.example.absen.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp
    )
)

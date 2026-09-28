package com.pixo.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PixoColors = darkColorScheme(
    primary = Color(0xFF9B6BFF),
    secondary = Color(0xFFFF5A9E),
    background = Color(0xFF090617),
    surface = Color(0xFF17112A),
    onBackground = Color(0xFFF7F2FF),
    onSurface = Color(0xFFF7F2FF)
)

@Composable
fun PixoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PixoColors,
        content = content
    )
}

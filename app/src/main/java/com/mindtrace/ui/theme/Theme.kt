package com.mindtrace.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mindtrace.util.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF356859),
    secondary = Color(0xFF7D5A50),
    tertiary = Color(0xFF5A6F8A),
    background = Color(0xFFF8FAF8),
    surface = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8CC7B0),
    secondary = Color(0xFFE3B4A6),
    tertiary = Color(0xFFAFC6E8),
    background = Color(0xFF111512),
    surface = Color(0xFF1A211D)
)

@Composable
fun MindTraceTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}

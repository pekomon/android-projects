package com.pekomon.lockbox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F6F73),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF6F5E38),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF6E5267),
    background = Color(0xFFF8FAF9),
    onBackground = Color(0xFF151C1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF151C1B),
    surfaceVariant = Color(0xFFDCE5E2),
    onSurfaceVariant = Color(0xFF3F4947),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF95D2D4),
    onPrimary = Color(0xFF003739),
    secondary = Color(0xFFD8C58E),
    onSecondary = Color(0xFF3B2F09),
    tertiary = Color(0xFFDCB8D0),
    background = Color(0xFF101414),
    onBackground = Color(0xFFE0E3E1),
    surface = Color(0xFF171C1B),
    onSurface = Color(0xFFE0E3E1),
    surfaceVariant = Color(0xFF3F4947),
    onSurfaceVariant = Color(0xFFBFC9C6),
    error = Color(0xFFFFB4AB),
)

@Composable
fun LockBoxTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}

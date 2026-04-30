package com.example.pekomon.weatherly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = DeepOcean,
    onPrimary = CloudTint,
    primaryContainer = SkyBlue,
    onPrimaryContainer = NightNavy,
    secondary = MistGray,
    onSecondary = CloudTint,
    background = CloudTint,
    onBackground = StormInk,
    surface = CloudTint,
    onSurface = StormInk,
    surfaceVariant = ColorTokens.surfaceVariantLight,
    onSurfaceVariant = MistGray,
)

private val DarkColors = darkColorScheme(
    primary = SkyBlue,
    onPrimary = NightNavy,
    primaryContainer = DeepOcean,
    onPrimaryContainer = CloudTint,
    secondary = ColorTokens.secondaryDark,
    onSecondary = NightNavy,
    background = NightNavy,
    onBackground = CloudTint,
    surface = ColorTokens.surfaceDark,
    onSurface = CloudTint,
    surfaceVariant = ColorTokens.surfaceVariantDark,
    onSurfaceVariant = ColorTokens.onSurfaceVariantDark,
)

@Composable
fun WeatherlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}

private object ColorTokens {
    val secondaryDark = Color(0xFF97A8BF)
    val surfaceDark = Color(0xFF0E1A2A)
    val surfaceVariantLight = Color(0xFFD7E7F7)
    val surfaceVariantDark = Color(0xFF1B2B40)
    val onSurfaceVariantDark = Color(0xFFB5C8E0)
}

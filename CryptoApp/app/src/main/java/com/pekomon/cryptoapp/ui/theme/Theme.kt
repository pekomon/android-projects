package com.pekomon.cryptoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = Ink900,
    secondary = Gold,
    tertiary = PositiveGreenDark,
    background = Ink900,
    onBackground = Chalk,
    surface = Ink800,
    onSurface = Chalk,
    surfaceVariant = Ink700,
    onSurfaceVariant = Paper100,
    outline = Color(0xFF596268),
    error = NegativeRedDark
)

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Chalk,
    secondary = Copper,
    tertiary = PositiveGreenLight,
    background = Paper50,
    onBackground = Ink900,
    surface = Chalk,
    onSurface = Ink900,
    surfaceVariant = Paper100,
    onSurfaceVariant = Ink700,
    outline = Color(0xFFC7BDAA),
    error = NegativeRedLight
)

@Composable
fun CryptoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes(
            small = CryptoShapes.control,
            medium = CryptoShapes.card,
            large = CryptoShapes.card
        ),
        content = content
    )
}

val ColorScheme.positiveGreen: Color
    get() = tertiary

val ColorScheme.negativeRed: Color
    get() = error

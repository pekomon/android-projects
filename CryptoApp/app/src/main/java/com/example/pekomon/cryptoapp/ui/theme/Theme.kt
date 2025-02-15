package com.example.pekomon.cryptoapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme

private val DarkColorScheme = darkColorScheme(
    primary = PositiveGreenDark,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    surface = Color(0xFF1C1B1F),
    background = Color(0xFF121212),
    onSurface = Color(0xFFFFFBFE),
    onBackground = Color(0xFFFFFBFE),
    error = NegativeRedDark
)

private val LightColorScheme = lightColorScheme(
    primary = PositiveGreenLight,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    surface = Color(0xFFFFFBFE),
    background = Color(0xFFF5F5F5),
    onSurface = Color(0xFF1C1B1F),
    onBackground = Color(0xFF1C1B1F),
    error = NegativeRedLight

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CryptoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes(),
        content = content
    )
}

val ColorScheme.positiveGreen: Color
    get() = Color(0xFF4CAF50)

val ColorScheme.negativeRed: Color
    get() = Color(0xFFE57373)
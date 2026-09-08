package com.pekomon.barcodelab.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0F7F62),
    onPrimary = Color.White,
    secondary = Color(0xFF575E71),
    background = Color(0xFFF7FAF7),
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF4ED7AA),
    onPrimary = Color(0xFF003828),
    secondary = Color(0xFFC0C6DC),
    background = Color(0xFF101411),
    surface = Color(0xFF191D1A),
    error = Color(0xFFFFB4AB),
)

@Composable
fun BarcodeLabTheme(
    colorScheme: ColorScheme = LightScheme,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}

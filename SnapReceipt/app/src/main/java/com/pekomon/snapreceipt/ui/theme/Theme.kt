package com.pekomon.snapreceipt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PineGreenDark,
    background = ForestNight,
    surface = MintNight,
    onPrimary = ForestNight,
    onBackground = MintPaper,
    onSurface = MintPaper,
    onSurfaceVariant = CloudGrayDark,
    outline = CloudGrayDark
)

private val LightColorScheme = lightColorScheme(
    primary = PineGreen,
    background = MintPaper,
    surface = ReceiptLine,
    onPrimary = MintPaper,
    onBackground = ReceiptInk,
    onSurface = ReceiptInk,
    onSurfaceVariant = CloudGray,
    outline = CloudGray
)

@Composable
fun SnapReceiptTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.pekomon.snapreceipt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LedgerGreenDark,
    secondary = CopperAccentDark,
    background = ForestNight,
    surface = PineNeedle,
    onPrimary = ForestNight,
    onBackground = ReceiptInkDark,
    onSurface = ReceiptInkDark,
    onSurfaceVariant = MutedOliveDark,
    outline = MutedOliveDark
)

private val LightColorScheme = lightColorScheme(
    primary = LedgerGreen,
    secondary = CopperAccent,
    background = PaperCanvas,
    surface = PaperCard,
    onPrimary = PaperCanvas,
    onBackground = ReceiptInk,
    onSurface = ReceiptInk,
    onSurfaceVariant = MutedOlive,
    outline = MutedOlive
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

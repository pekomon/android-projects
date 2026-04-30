package com.example.pekomon.weatherly.feature.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pekomon.weatherly.core.ui.PlaceholderScreen

@Composable
fun SettingsRoute(contentPadding: PaddingValues) {
    PlaceholderScreen(
        title = "Settings",
        body = "Units, appearance, and location permission status will be configured here.",
        modifier = Modifier.padding(contentPadding),
    )
}

package com.example.pekomon.weatherly.feature.favorites

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pekomon.weatherly.core.ui.PlaceholderScreen

@Composable
fun FavoritesRoute(contentPadding: PaddingValues) {
    PlaceholderScreen(
        title = "Favorites",
        body = "Saved locations and their quick-open weather details will live here.",
        modifier = Modifier.padding(contentPadding),
    )
}

package com.example.pekomon.weatherly.feature.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pekomon.weatherly.core.ui.PlaceholderScreen

@Composable
fun SearchRoute(contentPadding: PaddingValues) {
    PlaceholderScreen(
        title = "Search",
        body = "Location search, recent searches, and place-level weather detail will be built in the next slices.",
        modifier = Modifier.padding(contentPadding),
    )
}

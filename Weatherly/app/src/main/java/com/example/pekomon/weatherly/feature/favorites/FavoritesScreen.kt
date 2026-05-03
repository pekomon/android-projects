package com.example.pekomon.weatherly.feature.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.core.ui.LocationWeatherSummaryCard
import com.example.pekomon.weatherly.data.repository.currentLocation
import com.example.pekomon.weatherly.data.repository.sampleLocations
import com.example.pekomon.weatherly.data.repository.sampleWeatherDetails
import com.example.pekomon.weatherly.ui.theme.WeatherlyTheme

@Composable
fun FavoritesRoute(
    contentPadding: PaddingValues,
    viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModel.factory(context = LocalContext.current.applicationContext),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesScreen(
        uiState = uiState,
        onLocationSelected = viewModel::selectLocation,
        onRemoveFavorite = viewModel::removeFavorite,
        modifier = Modifier.padding(contentPadding),
    )
}

@Composable
internal fun FavoritesScreen(
    uiState: FavoritesUiState,
    onLocationSelected: (Location) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Saved locations stay here for quick weather checks and future home-screen shortcuts.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            uiState.errorMessage?.let { message ->
                item {
                    InfoCard(message = message)
                }
            }
            if (uiState.isLoadingSelection) {
                item {
                    LoadingCard(message = "Loading weather for the selected favorite…")
                }
            }
            uiState.selectedLocationWeather?.let { weatherDetails ->
                item {
                    LocationWeatherSummaryCard(
                        title = "Selected Favorite",
                        weatherDetails = weatherDetails,
                    )
                }
            }
            if (uiState.favorites.isEmpty()) {
                item {
                    InfoCard(message = "No saved places yet. Add favorites from Search to build your shortlist.")
                }
            } else {
                items(uiState.favorites, key = { it.id }) { location ->
                    FavoriteLocationCard(
                        location = location,
                        onClick = { onLocationSelected(location) },
                        onRemove = { onRemoveFavorite(location.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteLocationCard(
    location: Location,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = buildString {
                        location.adminRegion?.let {
                            append(it)
                            append(", ")
                        }
                        append(location.country)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = location.timezone,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remove favorite",
                )
            }
        }
    }
}

@Composable
private fun InfoCard(message: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun LoadingCard(message: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(strokeWidth = 2.dp)
            Text(
                text = message,
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    WeatherlyTheme {
        FavoritesScreen(
            uiState = FavoritesUiState(
                favorites = sampleLocations,
                selectedLocationWeather = sampleWeatherDetails(currentLocation),
            ),
            onLocationSelected = {},
            onRemoveFavorite = {},
        )
    }
}

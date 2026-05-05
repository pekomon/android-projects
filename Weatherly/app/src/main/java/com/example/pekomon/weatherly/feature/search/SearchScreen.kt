package com.example.pekomon.weatherly.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.data.repository.DataStoreSettingsRepository
import com.example.pekomon.weatherly.core.ui.LocationWeatherSummaryCard
import com.example.pekomon.weatherly.data.repository.currentLocation
import com.example.pekomon.weatherly.data.repository.sampleLocations
import com.example.pekomon.weatherly.data.repository.sampleWeatherDetails
import com.example.pekomon.weatherly.ui.theme.WeatherlyTheme

@Composable
fun SearchRoute(
    contentPadding: PaddingValues,
    viewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.factory(context = LocalContext.current.applicationContext),
    ),
) {
    val context = LocalContext.current.applicationContext
    val settingsRepository = remember(context) {
        DataStoreSettingsRepository(context)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()

    SearchScreen(
        uiState = uiState,
        settings = settings,
        onQueryChange = viewModel::updateQuery,
        onSearch = viewModel::searchNow,
        onClearQuery = viewModel::clearQuery,
        onLocationSelected = viewModel::selectLocation,
        onToggleFavorite = viewModel::toggleFavorite,
        modifier = Modifier.padding(contentPadding),
    )
}

@Composable
internal fun SearchScreen(
    uiState: SearchUiState,
    settings: com.example.pekomon.weatherly.core.model.AppSettings,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearQuery: () -> Unit,
    onLocationSelected: (Location) -> Unit,
    onToggleFavorite: (Location) -> Unit,
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
                        text = "Search Places",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Search any city, then preview current conditions and a short forecast before we wire full place detail.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                SearchInputCard(
                    query = uiState.query,
                    isSearching = uiState.isSearching,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    onClearQuery = onClearQuery,
                )
            }
            uiState.errorMessage?.let { message ->
                item {
                    MessageCard(message = message)
                }
            }
            if (uiState.isLoadingSelection) {
                item {
                    LoadingCard(message = "Loading forecast for the selected place…")
                }
            }
            uiState.selectedLocationWeather?.let { weatherDetails ->
                item {
                    LocationWeatherSummaryCard(
                        title = "Selected Place",
                        weatherDetails = weatherDetails,
                        settings = settings,
                        isFavorite = weatherDetails.location.id in uiState.favoriteLocationIds,
                        onToggleFavorite = { onToggleFavorite(weatherDetails.location) },
                    )
                }
            }
            if (uiState.results.isNotEmpty()) {
                item {
                    Text(
                        text = "Results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(uiState.results, key = { it.id }) { location ->
                    SearchResultCard(
                        location = location,
                        isFavorite = location.id in uiState.favoriteLocationIds,
                        onClick = { onLocationSelected(location) },
                        onToggleFavorite = { onToggleFavorite(location) },
                    )
                }
            } else if (uiState.hasSearched && !uiState.isSearching && uiState.errorMessage == null) {
                item {
                    MessageCard(message = "No locations found. Try a broader city or postal code search.")
                }
            }
        }
    }
}

@Composable
private fun SearchInputCard(
    query: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearQuery: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("City or postal code") },
                placeholder = { Text("Helsinki, Seattle, 00100…") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (query.isNotBlank()) {
                            IconButton(onClick = onClearQuery) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear search query",
                                )
                            }
                        }
                    }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { onSearch() },
                ),
            )
            Text(
                text = if (query.length >= 2) {
                    "Search starts automatically when you pause typing."
                } else {
                    "Type at least 2 characters. Press search on the keyboard to run immediately."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Open-Meteo geocoding, no API key required.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageCard(message: String) {
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
            CircularProgressIndicator(modifier = Modifier.width(22.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    location: Location,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
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
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) {
                            "Remove from favorites"
                        } else {
                            "Add to favorites"
                        },
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
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
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    WeatherlyTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "Hel",
                results = sampleLocations,
                selectedLocationWeather = sampleWeatherDetails(currentLocation),
                favoriteLocationIds = setOf(sampleLocations.first().id),
                hasSearched = true,
            ),
            settings = com.example.pekomon.weatherly.core.model.AppSettings(),
            onQueryChange = {},
            onSearch = {},
            onClearQuery = {},
            onLocationSelected = {},
            onToggleFavorite = {},
        )
    }
}

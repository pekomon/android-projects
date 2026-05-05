package com.example.pekomon.weatherly.feature.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pekomon.weatherly.core.model.AppSettings
import com.example.pekomon.weatherly.core.model.CurrentWeather
import com.example.pekomon.weatherly.core.model.DailyForecast
import com.example.pekomon.weatherly.core.model.HourlyForecast
import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.core.model.WeatherCondition
import com.example.pekomon.weatherly.core.model.WeatherDetails
import com.example.pekomon.weatherly.core.ui.formatTemperature
import com.example.pekomon.weatherly.core.ui.formatWindSpeed
import com.example.pekomon.weatherly.data.repository.DataStoreSettingsRepository
import com.example.pekomon.weatherly.data.repository.currentLocation
import com.example.pekomon.weatherly.data.repository.sampleWeatherDetails
import com.example.pekomon.weatherly.ui.theme.WeatherlyTheme
import java.time.format.DateTimeFormatter

@Composable
fun HomeRoute(
    contentPadding: PaddingValues,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory()),
) {
    val context = LocalContext.current.applicationContext
    val settingsRepository = remember(context) {
        DataStoreSettingsRepository(context)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        settings = settings,
        onRetry = viewModel::refresh,
        onEnableLocation = viewModel::refresh,
        modifier = Modifier.padding(contentPadding),
    )
}

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    settings: AppSettings,
    onRetry: () -> Unit,
    onEnableLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (uiState) {
            HomeUiState.Loading -> {
                StatePanel(
                    title = "Loading Local Weather",
                    body = "Fetching the latest conditions and short-term forecast for your current location.",
                    actionLabel = null,
                    onAction = null,
                    showProgress = true,
                )
            }

            HomeUiState.PermissionRequired -> {
                StatePanel(
                    title = "Location Access Needed",
                    body = "Weatherly uses your current position to show your local forecast on Home.",
                    actionLabel = "Enable location",
                    onAction = onEnableLocation,
                    showProgress = false,
                )
            }

            is HomeUiState.Error -> {
                StatePanel(
                    title = "Unable to Load Weather",
                    body = uiState.message,
                    actionLabel = "Try again",
                    onAction = onRetry,
                    showProgress = false,
                )
            }

            is HomeUiState.Loaded -> {
                LoadedHome(
                    weatherDetails = uiState.weatherDetails,
                    settings = settings,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun LoadedHome(
    weatherDetails: WeatherDetails,
    settings: AppSettings,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            HomeHeader(location = weatherDetails.location)
        }
        item {
            CurrentWeatherCard(
                location = weatherDetails.location,
                currentWeather = weatherDetails.currentWeather,
                todayForecast = weatherDetails.dailyForecast.firstOrNull(),
                settings = settings,
            )
        }
        item {
            MetricGrid(
                currentWeather = weatherDetails.currentWeather,
                settings = settings,
            )
        }
        item {
            ForecastSectionTitle(title = "Hourly Forecast", subtitle = "Next 12 hours")
        }
        item {
            HourlyForecastRow(
                items = weatherDetails.hourlyForecast,
                settings = settings,
            )
        }
        item {
            ForecastSectionTitle(title = "Daily Forecast", subtitle = "Next 7 days")
        }
        items(weatherDetails.dailyForecast) { forecast ->
            DailyForecastCard(
                forecast = forecast,
                settings = settings,
            )
        }
    }
}

@Composable
private fun HomeHeader(location: Location) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Weather",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = buildString {
                append(location.name)
                location.adminRegion?.let {
                    append(", ")
                    append(it)
                }
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CurrentWeatherCard(
    location: Location,
    currentWeather: CurrentWeather,
    todayForecast: DailyForecast?,
    settings: AppSettings,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            WeatherConditionPill(condition = currentWeather.condition)
            Text(
                text = formatTemperature(currentWeather.temperature, settings),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = buildString {
                    append(conditionLabel(currentWeather.condition))
                    append(" in ")
                    append(location.name)
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "Feels like ${formatTemperature(currentWeather.apparentTemperature, settings)}. Wind ${formatWindSpeed(currentWeather.windSpeed, settings)}.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
            )
            todayForecast?.let { forecast ->
                Text(
                    text = "Today’s range ${formatTemperature(forecast.minTemperature, settings)} to ${formatTemperature(forecast.maxTemperature, settings)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                )
            }
        }
    }
}

@Composable
private fun WeatherConditionPill(condition: WeatherCondition) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = conditionLabel(condition),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun MetricGrid(
    currentWeather: CurrentWeather,
    settings: AppSettings,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Humidity",
                value = "${currentWeather.humidityPercent}%",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Pressure",
                value = "${currentWeather.pressureHpa.toInt()} hPa",
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Visibility",
                value = currentWeather.visibilityKilometers?.let { "${it.toInt()} km" } ?: "N/A",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "UV Index",
                value = currentWeather.uvIndex?.let { String.format("%.1f", it) } ?: "N/A",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun ForecastSectionTitle(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HourlyForecastRow(
    items: List<HourlyForecast>,
    settings: AppSettings,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items) { item ->
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .width(112.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = item.dateTime.format(hourFormatter),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatTemperature(item.temperature, settings),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = conditionShortLabel(item.condition),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "${(item.precipitationChance * 100).toInt()}% rain",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastCard(
    forecast: DailyForecast,
    settings: AppSettings,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = forecast.date.format(dayFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = conditionShortLabel(forecast.condition),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${formatTemperature(forecast.minTemperature, settings)} / ${formatTemperature(forecast.maxTemperature, settings)}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "${(forecast.precipitationChance * 100).toInt()}% precip",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatePanel(
    title: String,
    body: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    showProgress: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (showProgress) {
                    CircularProgressIndicator()
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (actionLabel != null && onAction != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = onAction) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}

private fun conditionLabel(condition: WeatherCondition): String = when (condition) {
    WeatherCondition.Clear -> "Clear sky"
    WeatherCondition.MostlyClear -> "Mostly clear"
    WeatherCondition.PartlyCloudy -> "Partly cloudy"
    WeatherCondition.Cloudy -> "Cloudy"
    WeatherCondition.Fog -> "Foggy"
    WeatherCondition.Drizzle -> "Drizzle"
    WeatherCondition.Rain -> "Rain"
    WeatherCondition.Snow -> "Snow"
    WeatherCondition.Thunderstorm -> "Thunderstorm"
    WeatherCondition.Windy -> "Windy"
    WeatherCondition.Unknown -> "Unknown conditions"
}

private fun conditionShortLabel(condition: WeatherCondition): String = when (condition) {
    WeatherCondition.Clear -> "Clear"
    WeatherCondition.MostlyClear -> "Mostly clear"
    WeatherCondition.PartlyCloudy -> "Partly cloudy"
    WeatherCondition.Cloudy -> "Cloudy"
    WeatherCondition.Fog -> "Fog"
    WeatherCondition.Drizzle -> "Drizzle"
    WeatherCondition.Rain -> "Rain"
    WeatherCondition.Snow -> "Snow"
    WeatherCondition.Thunderstorm -> "Storm"
    WeatherCondition.Windy -> "Windy"
    WeatherCondition.Unknown -> "Unknown"
}

private val hourFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

@Preview(showBackground = true)
@Composable
private fun LoadedHomePreview() {
    WeatherlyTheme {
        HomeScreen(
            uiState = HomeUiState.Loaded(sampleWeatherDetails(currentLocation)),
            settings = AppSettings(),
            onRetry = {},
            onEnableLocation = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingHomePreview() {
    WeatherlyTheme {
        HomeScreen(
            uiState = HomeUiState.Loading,
            settings = AppSettings(),
            onRetry = {},
            onEnableLocation = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionHomePreview() {
    WeatherlyTheme {
        HomeScreen(
            uiState = HomeUiState.PermissionRequired,
            settings = AppSettings(),
            onRetry = {},
            onEnableLocation = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorHomePreview() {
    WeatherlyTheme {
        HomeScreen(
            uiState = HomeUiState.Error("Open-Meteo is unavailable right now. Please try again in a moment."),
            settings = AppSettings(),
            onRetry = {},
            onEnableLocation = {},
        )
    }
}

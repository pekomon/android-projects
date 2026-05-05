package com.example.pekomon.weatherly.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pekomon.weatherly.core.model.AppSettings
import com.example.pekomon.weatherly.core.model.WeatherCondition
import com.example.pekomon.weatherly.core.model.WeatherDetails

@Composable
fun LocationWeatherSummaryCard(
    title: String,
    weatherDetails: WeatherDetails,
    settings: AppSettings,
    isFavorite: Boolean? = null,
    onToggleFavorite: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val location = weatherDetails.location
    val current = weatherDetails.currentWeather
    val nextDaily = weatherDetails.dailyForecast.take(3)

    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                if (isFavorite != null && onToggleFavorite != null) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = if (isFavorite) {
                                "Remove from favorites"
                            } else {
                                "Add to favorites"
                            },
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            Text(
                text = "${location.name}, ${location.country}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "${formatTemperature(current.temperature, settings)}  •  ${conditionLabel(current.condition)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "Feels like ${formatTemperature(current.apparentTemperature, settings)}. Wind ${formatWindSpeed(current.windSpeed, settings)}. Humidity ${current.humidityPercent}%.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
            )
            if (nextDaily.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Next 3 days",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                nextDaily.forEach { forecast ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = forecast.date.dayOfWeek.name.lowercase().replaceFirstChar(Char::titlecase),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "${formatTemperature(forecast.minTemperature, settings)} / ${formatTemperature(forecast.maxTemperature, settings)}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
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

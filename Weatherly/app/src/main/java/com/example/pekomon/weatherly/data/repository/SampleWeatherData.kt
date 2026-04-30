package com.example.pekomon.weatherly.data.repository

import com.example.pekomon.weatherly.core.model.CurrentWeather
import com.example.pekomon.weatherly.core.model.DailyForecast
import com.example.pekomon.weatherly.core.model.HourlyForecast
import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.core.model.WeatherCondition
import com.example.pekomon.weatherly.core.model.WeatherDetails
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

internal val currentLocation = Location(
    id = "helsinki-fi",
    name = "Helsinki",
    adminRegion = "Uusimaa",
    country = "Finland",
    latitude = 60.1699,
    longitude = 24.9384,
    timezone = "Europe/Helsinki",
)

internal val sampleLocations = listOf(
    currentLocation,
    Location(
        id = "turku-fi",
        name = "Turku",
        adminRegion = "Southwest Finland",
        country = "Finland",
        latitude = 60.4518,
        longitude = 22.2666,
        timezone = "Europe/Helsinki",
    ),
    Location(
        id = "tokyo-jp",
        name = "Tokyo",
        adminRegion = "Tokyo",
        country = "Japan",
        latitude = 35.6764,
        longitude = 139.6500,
        timezone = "Asia/Tokyo",
    ),
    Location(
        id = "seattle-us",
        name = "Seattle",
        adminRegion = "Washington",
        country = "United States",
        latitude = 47.6062,
        longitude = -122.3321,
        timezone = "America/Los_Angeles",
    ),
)

internal fun sampleWeatherDetails(location: Location): WeatherDetails {
    val zoneId = ZoneId.of(location.timezone)
    val now = ZonedDateTime.now(zoneId).withMinute(0).withSecond(0).withNano(0)

    return WeatherDetails(
        location = location,
        currentWeather = CurrentWeather(
            temperature = 14.0,
            apparentTemperature = 12.5,
            condition = WeatherCondition.PartlyCloudy,
            weatherCode = 2,
            windSpeed = 19.0,
            humidityPercent = 61,
            pressureHpa = 1012.0,
            visibilityKilometers = 12.0,
            uvIndex = 2.4,
        ),
        hourlyForecast = List(12) { index ->
            HourlyForecast(
                dateTime = now.plusHours(index.toLong()),
                temperature = 14.0 - (index * 0.3),
                precipitationChance = when {
                    index in 4..6 -> 0.45
                    index in 7..8 -> 0.20
                    else -> 0.05
                },
                condition = when {
                    index < 3 -> WeatherCondition.PartlyCloudy
                    index in 4..6 -> WeatherCondition.Rain
                    index > 8 -> WeatherCondition.Cloudy
                    else -> WeatherCondition.MostlyClear
                },
            )
        },
        dailyForecast = List(7) { index ->
            DailyForecast(
                date = LocalDate.now(zoneId).plusDays(index.toLong()),
                minTemperature = 8.0 + index,
                maxTemperature = 14.0 + index,
                precipitationChance = when (index) {
                    1, 4 -> 0.60
                    2 -> 0.35
                    else -> 0.10
                },
                condition = when (index) {
                    1, 4 -> WeatherCondition.Rain
                    2 -> WeatherCondition.Cloudy
                    5 -> WeatherCondition.Windy
                    else -> WeatherCondition.PartlyCloudy
                },
            )
        },
        fetchedAt = now,
    )
}

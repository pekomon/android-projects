package com.pekomon.weatherly.testing

import com.pekomon.weatherly.core.model.CurrentWeather
import com.pekomon.weatherly.core.model.DailyForecast
import com.pekomon.weatherly.core.model.HourlyForecast
import com.pekomon.weatherly.core.model.Location
import com.pekomon.weatherly.core.model.WeatherCondition
import com.pekomon.weatherly.core.model.WeatherDetails
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

val composeTestLocation = Location(
    id = "helsinki-fi",
    name = "Helsinki",
    adminRegion = "Uusimaa",
    country = "Finland",
    latitude = 60.1699,
    longitude = 24.9384,
    timezone = "Europe/Helsinki",
)

val composeSecondLocation = Location(
    id = "turku-fi",
    name = "Turku",
    adminRegion = "Southwest Finland",
    country = "Finland",
    latitude = 60.4518,
    longitude = 22.2666,
    timezone = "Europe/Helsinki",
)

fun composeWeatherDetails(location: Location = composeTestLocation): WeatherDetails {
    val zoneId = ZoneId.of(location.timezone)
    val fetchedAt = ZonedDateTime.of(2026, 7, 24, 12, 0, 0, 0, zoneId)
    return WeatherDetails(
        location = location,
        currentWeather = CurrentWeather(
            temperature = 18.4,
            apparentTemperature = 17.9,
            condition = WeatherCondition.PartlyCloudy,
            weatherCode = 2,
            windSpeed = 11.2,
            humidityPercent = 64,
            pressureHpa = 1012.5,
            visibilityKilometers = 24.0,
            uvIndex = 4.8,
        ),
        hourlyForecast = listOf(
            HourlyForecast(fetchedAt, 18.4, 0.0, WeatherCondition.Clear),
            HourlyForecast(fetchedAt.plusHours(1), 19.1, 0.1, WeatherCondition.Cloudy),
        ),
        dailyForecast = listOf(
            DailyForecast(LocalDate.of(2026, 7, 24), 12.0, 20.5, 0.3, WeatherCondition.PartlyCloudy),
            DailyForecast(LocalDate.of(2026, 7, 25), 13.5, 21.2, 0.65, WeatherCondition.Rain),
        ),
        fetchedAt = fetchedAt,
    )
}

package com.example.pekomon.weatherly.core.model

import java.time.ZonedDateTime

data class WeatherDetails(
    val location: Location,
    val currentWeather: CurrentWeather,
    val hourlyForecast: List<HourlyForecast>,
    val dailyForecast: List<DailyForecast>,
    val fetchedAt: ZonedDateTime,
)

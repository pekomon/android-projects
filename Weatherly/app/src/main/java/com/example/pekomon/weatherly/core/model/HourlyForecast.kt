package com.example.pekomon.weatherly.core.model

import java.time.ZonedDateTime

data class HourlyForecast(
    val dateTime: ZonedDateTime,
    val temperature: Double,
    val precipitationChance: Double,
    val condition: WeatherCondition,
)

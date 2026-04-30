package com.example.pekomon.weatherly.core.model

import java.time.LocalDate

data class DailyForecast(
    val date: LocalDate,
    val minTemperature: Double,
    val maxTemperature: Double,
    val precipitationChance: Double,
    val condition: WeatherCondition,
)

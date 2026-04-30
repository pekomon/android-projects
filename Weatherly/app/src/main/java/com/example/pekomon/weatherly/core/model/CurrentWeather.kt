package com.example.pekomon.weatherly.core.model

data class CurrentWeather(
    val temperature: Double,
    val apparentTemperature: Double,
    val condition: WeatherCondition,
    val weatherCode: Int,
    val windSpeed: Double,
    val humidityPercent: Int,
    val pressureHpa: Double,
    val visibilityKilometers: Double? = null,
    val uvIndex: Double? = null,
)

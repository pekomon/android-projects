package com.example.pekomon.weatherly.core.model

enum class TemperatureUnit(
    val symbol: String,
    val settingsLabel: String,
) {
    Celsius(symbol = "C", settingsLabel = "Celsius"),
    Fahrenheit(symbol = "F", settingsLabel = "Fahrenheit"),
}

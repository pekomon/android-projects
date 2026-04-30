package com.example.pekomon.weatherly.core.model

enum class WindSpeedUnit(
    val symbol: String,
    val settingsLabel: String,
) {
    KilometersPerHour(symbol = "km/h", settingsLabel = "km/h"),
    MetersPerSecond(symbol = "m/s", settingsLabel = "m/s"),
    MilesPerHour(symbol = "mph", settingsLabel = "mph"),
}

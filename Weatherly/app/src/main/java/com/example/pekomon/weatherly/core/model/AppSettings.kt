package com.example.pekomon.weatherly.core.model

data class AppSettings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.Celsius,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KilometersPerHour,
    val appearanceMode: AppAppearanceMode = AppAppearanceMode.System,
)

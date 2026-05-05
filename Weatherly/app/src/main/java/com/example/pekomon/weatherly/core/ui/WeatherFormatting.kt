package com.example.pekomon.weatherly.core.ui

import com.example.pekomon.weatherly.core.model.AppSettings
import com.example.pekomon.weatherly.core.model.TemperatureUnit
import com.example.pekomon.weatherly.core.model.WindSpeedUnit
import kotlin.math.roundToInt

fun formatTemperature(
    temperatureCelsius: Double,
    settings: AppSettings,
    includeUnit: Boolean = false,
): String {
    val converted = when (settings.temperatureUnit) {
        TemperatureUnit.Celsius -> temperatureCelsius
        TemperatureUnit.Fahrenheit -> (temperatureCelsius * 9.0 / 5.0) + 32.0
    }.roundToInt()

    return if (includeUnit) {
        "$converted°${settings.temperatureUnit.symbol}"
    } else {
        "$converted°"
    }
}

fun formatWindSpeed(
    kilometersPerHour: Double,
    settings: AppSettings,
): String {
    val converted = when (settings.windSpeedUnit) {
        WindSpeedUnit.KilometersPerHour -> kilometersPerHour
        WindSpeedUnit.MetersPerSecond -> kilometersPerHour / 3.6
        WindSpeedUnit.MilesPerHour -> kilometersPerHour / 1.609344
    }

    val value = when (settings.windSpeedUnit) {
        WindSpeedUnit.KilometersPerHour -> converted.roundToInt().toString()
        WindSpeedUnit.MetersPerSecond -> String.format("%.1f", converted)
        WindSpeedUnit.MilesPerHour -> converted.roundToInt().toString()
    }

    return "$value ${settings.windSpeedUnit.symbol}"
}

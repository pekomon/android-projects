package com.pekomon.weatherly.core.ui

import com.pekomon.weatherly.core.model.AppSettings
import com.pekomon.weatherly.core.model.TemperatureUnit
import com.pekomon.weatherly.core.model.WindSpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherFormattingTest {
    @Test
    fun formatsCelsiusTemperatureWithRoundingAndOptionalUnit() {
        val settings = AppSettings(temperatureUnit = TemperatureUnit.Celsius)

        assertEquals("22°", formatTemperature(21.6, settings))
        assertEquals("-3°C", formatTemperature(-2.6, settings, includeUnit = true))
    }

    @Test
    fun formatsFahrenheitTemperatureWithRoundingAndUnit() {
        val settings = AppSettings(temperatureUnit = TemperatureUnit.Fahrenheit)

        assertEquals("71°", formatTemperature(21.6, settings))
        assertEquals("27°F", formatTemperature(-2.6, settings, includeUnit = true))
    }

    @Test
    fun formatsWindSpeedInSupportedUnits() {
        assertEquals(
            "12 km/h",
            formatWindSpeed(12.4, AppSettings(windSpeedUnit = WindSpeedUnit.KilometersPerHour)),
        )
        assertEquals(
            "3.4 m/s",
            formatWindSpeed(12.4, AppSettings(windSpeedUnit = WindSpeedUnit.MetersPerSecond)),
        )
        assertEquals(
            "8 mph",
            formatWindSpeed(12.4, AppSettings(windSpeedUnit = WindSpeedUnit.MilesPerHour)),
        )
    }
}

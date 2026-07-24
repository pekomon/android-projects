package com.pekomon.weatherly.data.repository

import com.pekomon.weatherly.core.model.Location
import com.pekomon.weatherly.core.model.WeatherCondition
import java.time.LocalDate
import java.time.ZoneId
import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpenMeteoWeatherMapperTest {
    private val mapper = OpenMeteoWeatherMapper()
    private val location = Location(
        id = "helsinki",
        name = "Helsinki",
        adminRegion = "Uusimaa",
        country = "Finland",
        latitude = 60.1699,
        longitude = 24.9384,
        timezone = "Europe/Helsinki",
    )

    @Test
    fun mapsCurrentHourlyAndDailyForecasts() {
        val weather = mapper.map(location, resourceText("openmeteo/forecast.json"))

        assertEquals(location, weather.location)
        assertEquals(18.4, weather.currentWeather.temperature, 0.001)
        assertEquals(17.9, weather.currentWeather.apparentTemperature, 0.001)
        assertEquals(WeatherCondition.PartlyCloudy, weather.currentWeather.condition)
        assertEquals(2, weather.currentWeather.weatherCode)
        assertEquals(11.2, weather.currentWeather.windSpeed, 0.001)
        assertEquals(64, weather.currentWeather.humidityPercent)
        assertEquals(1012.5, weather.currentWeather.pressureHpa, 0.001)
        assertEquals(24.0, weather.currentWeather.visibilityKilometers)
        assertEquals(4.8, weather.currentWeather.uvIndex)
        assertEquals(ZoneId.of("Europe/Helsinki"), weather.fetchedAt.zone)

        assertEquals(3, weather.hourlyForecast.size)
        assertEquals(WeatherCondition.Clear, weather.hourlyForecast[0].condition)
        assertEquals(0.1, weather.hourlyForecast[1].precipitationChance, 0.001)
        assertEquals(WeatherCondition.Rain, weather.hourlyForecast[2].condition)

        assertEquals(2, weather.dailyForecast.size)
        assertEquals(LocalDate.parse("2026-07-24"), weather.dailyForecast[0].date)
        assertEquals(WeatherCondition.Cloudy, weather.dailyForecast[0].condition)
        assertEquals(0.65, weather.dailyForecast[1].precipitationChance, 0.001)
    }

    @Test
    fun mapsOptionalCurrentFieldsWhenTheyAreMissing() {
        val weather = mapper.map(location, resourceText("openmeteo/forecast_without_optional_fields.json"))

        assertNull(weather.currentWeather.visibilityKilometers)
        assertNull(weather.currentWeather.uvIndex)
    }

    @Test
    fun mapsWeatherCodeFamiliesAndUnknownValues() {
        assertEquals(WeatherCondition.Clear, mapper.weatherConditionForCode(0))
        assertEquals(WeatherCondition.MostlyClear, mapper.weatherConditionForCode(1))
        assertEquals(WeatherCondition.PartlyCloudy, mapper.weatherConditionForCode(2))
        assertEquals(WeatherCondition.Cloudy, mapper.weatherConditionForCode(3))
        assertEquals(WeatherCondition.Fog, mapper.weatherConditionForCode(45))
        assertEquals(WeatherCondition.Drizzle, mapper.weatherConditionForCode(53))
        assertEquals(WeatherCondition.Rain, mapper.weatherConditionForCode(80))
        assertEquals(WeatherCondition.Snow, mapper.weatherConditionForCode(85))
        assertEquals(WeatherCondition.Thunderstorm, mapper.weatherConditionForCode(95))
        assertEquals(WeatherCondition.Unknown, mapper.weatherConditionForCode(1234))
    }

    @Test(expected = JSONException::class)
    fun malformedRequiredDataThrows() {
        mapper.map(location, """{"current":{},"hourly":{},"daily":{}}""")
    }
}

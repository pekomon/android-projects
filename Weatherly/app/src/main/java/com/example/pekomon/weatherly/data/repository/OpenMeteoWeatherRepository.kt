package com.example.pekomon.weatherly.data.repository

import com.example.pekomon.weatherly.core.model.CurrentWeather
import com.example.pekomon.weatherly.core.model.DailyForecast
import com.example.pekomon.weatherly.core.model.HourlyForecast
import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.core.model.WeatherCondition
import com.example.pekomon.weatherly.core.model.WeatherDetails
import com.example.pekomon.weatherly.data.remote.OpenMeteoWeatherApi
import com.example.pekomon.weatherly.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class OpenMeteoWeatherRepository(
    private val api: OpenMeteoWeatherApi = OpenMeteoWeatherApi(),
) : WeatherRepository {
    override suspend fun getWeatherDetails(location: Location): WeatherDetails = withContext(Dispatchers.IO) {
        val payload = api.getForecast(
            latitude = location.latitude,
            longitude = location.longitude,
            timezone = location.timezone,
        )
        parseWeatherDetails(location, payload)
    }

    override suspend fun getCurrentLocationWeather(): WeatherDetails {
        return getWeatherDetails(currentLocation)
    }

    private fun parseWeatherDetails(
        location: Location,
        payload: String,
    ): WeatherDetails {
        val root = JSONObject(payload)
        val current = root.getJSONObject("current")
        val hourly = root.getJSONObject("hourly")
        val daily = root.getJSONObject("daily")
        val zoneId = ZoneId.of(location.timezone)

        return WeatherDetails(
            location = location,
            currentWeather = CurrentWeather(
                temperature = current.requireDouble("temperature_2m"),
                apparentTemperature = current.requireDouble("apparent_temperature"),
                condition = weatherConditionForCode(current.requireInt("weather_code")),
                weatherCode = current.requireInt("weather_code"),
                windSpeed = current.requireDouble("wind_speed_10m"),
                humidityPercent = current.requireInt("relative_humidity_2m"),
                pressureHpa = current.requireDouble("pressure_msl"),
                visibilityKilometers = current.optionalDouble("visibility")?.div(1000.0),
                uvIndex = current.optionalDouble("uv_index"),
            ),
            hourlyForecast = parseHourlyForecast(hourly, zoneId),
            dailyForecast = parseDailyForecast(daily),
            fetchedAt = LocalDateTime.parse(current.getString("time")).atZone(zoneId),
        )
    }

    private fun parseHourlyForecast(
        hourly: JSONObject,
        zoneId: ZoneId,
    ): List<HourlyForecast> {
        val times = hourly.getJSONArray("time")
        val temperatures = hourly.getJSONArray("temperature_2m")
        val precipitationProbabilities = hourly.getJSONArray("precipitation_probability")
        val weatherCodes = hourly.getJSONArray("weather_code")

        return List(times.length()) { index ->
            val weatherCode = weatherCodes.requireInt(index)
            HourlyForecast(
                dateTime = LocalDateTime.parse(times.getString(index)).atZone(zoneId),
                temperature = temperatures.requireDouble(index),
                precipitationChance = precipitationProbabilities.requireDouble(index) / 100.0,
                condition = weatherConditionForCode(weatherCode),
            )
        }
    }

    private fun parseDailyForecast(daily: JSONObject): List<DailyForecast> {
        val dates = daily.getJSONArray("time")
        val weatherCodes = daily.getJSONArray("weather_code")
        val minTemperatures = daily.getJSONArray("temperature_2m_min")
        val maxTemperatures = daily.getJSONArray("temperature_2m_max")
        val precipitationProbabilities = daily.getJSONArray("precipitation_probability_max")

        return List(dates.length()) { index ->
            val weatherCode = weatherCodes.requireInt(index)
            DailyForecast(
                date = LocalDate.parse(dates.getString(index)),
                minTemperature = minTemperatures.requireDouble(index),
                maxTemperature = maxTemperatures.requireDouble(index),
                precipitationChance = precipitationProbabilities.requireDouble(index) / 100.0,
                condition = weatherConditionForCode(weatherCode),
            )
        }
    }

    private fun weatherConditionForCode(code: Int): WeatherCondition = when (code) {
        0 -> WeatherCondition.Clear
        1 -> WeatherCondition.MostlyClear
        2 -> WeatherCondition.PartlyCloudy
        3 -> WeatherCondition.Cloudy
        45, 48 -> WeatherCondition.Fog
        51, 53, 55, 56, 57 -> WeatherCondition.Drizzle
        61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.Rain
        71, 73, 75, 77, 85, 86 -> WeatherCondition.Snow
        95, 96, 99 -> WeatherCondition.Thunderstorm
        else -> WeatherCondition.Unknown
    }
}

private fun JSONObject.requireDouble(name: String): Double = getDouble(name)

private fun JSONObject.requireInt(name: String): Int = getInt(name)

private fun JSONObject.optionalDouble(name: String): Double? {
    if (isNull(name)) return null
    return optDouble(name).takeUnless(Double::isNaN)
}

private fun JSONArray.requireDouble(index: Int): Double = getDouble(index)

private fun JSONArray.requireInt(index: Int): Int = getInt(index)

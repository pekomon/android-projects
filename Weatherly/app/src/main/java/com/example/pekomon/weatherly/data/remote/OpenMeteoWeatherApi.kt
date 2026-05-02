package com.example.pekomon.weatherly.data.remote

import android.net.Uri
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class OpenMeteoWeatherApi(
    private val baseUrl: String = FORECAST_BASE_URL,
) {
    fun getForecast(
        latitude: Double,
        longitude: Double,
        timezone: String,
    ): String {
        val requestUrl = Uri.parse(baseUrl).buildUpon()
            .appendQueryParameter("latitude", latitude.toString())
            .appendQueryParameter("longitude", longitude.toString())
            .appendQueryParameter("timezone", timezone)
            .appendQueryParameter(
                "current",
                "temperature_2m,apparent_temperature,weather_code,wind_speed_10m,relative_humidity_2m,pressure_msl,visibility,uv_index",
            )
            .appendQueryParameter(
                "hourly",
                "temperature_2m,precipitation_probability,weather_code",
            )
            .appendQueryParameter(
                "daily",
                "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",
            )
            .appendQueryParameter("forecast_hours", "12")
            .appendQueryParameter("forecast_days", "7")
            .build()
            .toString()

        val connection = URL(requestUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/json")

        return try {
            val responseCode = connection.responseCode
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = inputStream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            if (responseCode !in 200..299) {
                throw IllegalStateException(
                    "Open-Meteo request failed with HTTP $responseCode" +
                        if (body.isNotBlank()) ": $body" else ".",
                )
            }
            body
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val FORECAST_BASE_URL = "https://api.open-meteo.com/v1/forecast"
    }
}

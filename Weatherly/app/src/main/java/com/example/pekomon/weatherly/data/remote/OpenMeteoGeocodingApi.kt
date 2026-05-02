package com.example.pekomon.weatherly.data.remote

import android.net.Uri
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class OpenMeteoGeocodingApi(
    private val baseUrl: String = GEOCODING_BASE_URL,
) {
    fun searchLocations(
        query: String,
        count: Int = DEFAULT_RESULT_COUNT,
        language: String = DEFAULT_LANGUAGE,
    ): String {
        val requestUrl = Uri.parse(baseUrl).buildUpon()
            .appendQueryParameter("name", query)
            .appendQueryParameter("count", count.toString())
            .appendQueryParameter("language", language)
            .appendQueryParameter("format", "json")
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
                    "Open-Meteo geocoding failed with HTTP $responseCode" +
                        if (body.isNotBlank()) ": $body" else ".",
                )
            }
            body
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/v1/search"
        const val DEFAULT_RESULT_COUNT = 8
        const val DEFAULT_LANGUAGE = "en"
    }
}

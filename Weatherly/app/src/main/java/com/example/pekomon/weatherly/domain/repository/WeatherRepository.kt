package com.example.pekomon.weatherly.domain.repository

import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.core.model.WeatherDetails

interface WeatherRepository {
    suspend fun getWeatherDetails(location: Location): WeatherDetails

    suspend fun getCurrentLocationWeather(): WeatherDetails
}

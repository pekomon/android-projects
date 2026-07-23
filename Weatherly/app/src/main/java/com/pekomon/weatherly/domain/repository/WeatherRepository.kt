package com.pekomon.weatherly.domain.repository

import com.pekomon.weatherly.core.model.Location
import com.pekomon.weatherly.core.model.WeatherDetails

interface WeatherRepository {
    suspend fun getWeatherDetails(location: Location): WeatherDetails

    suspend fun getCurrentLocationWeather(): WeatherDetails
}

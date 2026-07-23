package com.pekomon.weatherly.data.repository

import com.pekomon.weatherly.core.model.Location
import com.pekomon.weatherly.core.model.WeatherDetails
import com.pekomon.weatherly.domain.repository.WeatherRepository
import kotlinx.coroutines.delay

class FakeWeatherRepository : WeatherRepository {
    override suspend fun getWeatherDetails(location: Location): WeatherDetails {
        delay(400)
        return sampleWeatherDetails(location)
    }

    override suspend fun getCurrentLocationWeather(): WeatherDetails {
        delay(400)
        return sampleWeatherDetails(currentLocation)
    }
}

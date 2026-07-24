package com.pekomon.weatherly.testing

import com.pekomon.weatherly.core.model.Location
import com.pekomon.weatherly.core.model.WeatherDetails
import com.pekomon.weatherly.domain.repository.FavoritesRepository
import com.pekomon.weatherly.domain.repository.LocationSearchRepository
import com.pekomon.weatherly.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWeatherRepository(
    private val currentWeatherResults: ArrayDeque<Result<WeatherDetails>> = ArrayDeque(),
    private val locationWeatherResults: ArrayDeque<Result<WeatherDetails>> = ArrayDeque(),
) : WeatherRepository {
    var currentRequestCount = 0
        private set
    var locationRequestCount = 0
        private set

    override suspend fun getWeatherDetails(location: Location): WeatherDetails {
        locationRequestCount += 1
        return (locationWeatherResults.removeFirstOrNull() ?: Result.success(testWeatherDetails(location))).getOrThrow()
    }

    override suspend fun getCurrentLocationWeather(): WeatherDetails {
        currentRequestCount += 1
        return (currentWeatherResults.removeFirstOrNull() ?: Result.success(testWeatherDetails())).getOrThrow()
    }
}

class FakeLocationSearchRepository(
    private val resultsByQuery: MutableMap<String, Result<List<Location>>> = mutableMapOf(),
) : LocationSearchRepository {
    val queries = mutableListOf<String>()

    fun resultFor(query: String, result: Result<List<Location>>) {
        resultsByQuery[query] = result
    }

    override suspend fun searchLocations(query: String): List<Location> {
        queries += query
        return (resultsByQuery[query] ?: Result.success(emptyList())).getOrThrow()
    }
}

class FakeFavoritesRepository(initialFavorites: List<Location> = emptyList()) : FavoritesRepository {
    private val favoriteState = MutableStateFlow(initialFavorites)
    override val favorites = favoriteState

    override suspend fun addFavorite(location: Location) {
        if (favoriteState.value.none { it.id == location.id }) {
            favoriteState.value = favoriteState.value + location
        }
    }

    override suspend fun removeFavorite(locationId: String) {
        favoriteState.value = favoriteState.value.filterNot { it.id == locationId }
    }

    override suspend fun toggleFavorite(location: Location) {
        if (favoriteState.value.any { it.id == location.id }) {
            removeFavorite(location.id)
        } else {
            addFavorite(location)
        }
    }
}

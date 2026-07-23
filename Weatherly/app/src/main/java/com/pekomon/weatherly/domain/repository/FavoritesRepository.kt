package com.pekomon.weatherly.domain.repository

import com.pekomon.weatherly.core.model.Location
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    val favorites: Flow<List<Location>>

    suspend fun addFavorite(location: Location)

    suspend fun removeFavorite(locationId: String)

    suspend fun toggleFavorite(location: Location)
}

package com.example.pekomon.weatherly.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class DataStoreFavoritesRepository(
    context: Context,
) : FavoritesRepository {
    private val dataStore = context.weatherlyDataStore

    override val favorites: Flow<List<Location>> = dataStore.data.map { preferences ->
        parseLocations(preferences[FAVORITES_KEY].orEmpty())
    }

    override suspend fun addFavorite(location: Location) {
        dataStore.edit { preferences ->
            val current = parseLocations(preferences[FAVORITES_KEY].orEmpty())
            val next = if (current.any { it.id == location.id }) current else current + location
            preferences[FAVORITES_KEY] = serializeLocations(next)
        }
    }

    override suspend fun removeFavorite(locationId: String) {
        dataStore.edit { preferences ->
            val current = parseLocations(preferences[FAVORITES_KEY].orEmpty())
            val next = current.filterNot { it.id == locationId }
            preferences[FAVORITES_KEY] = serializeLocations(next)
        }
    }

    override suspend fun toggleFavorite(location: Location) {
        dataStore.edit { preferences ->
            val current = parseLocations(preferences[FAVORITES_KEY].orEmpty())
            val exists = current.any { it.id == location.id }
            val next = if (exists) {
                current.filterNot { it.id == location.id }
            } else {
                current + location
            }
            preferences[FAVORITES_KEY] = serializeLocations(next)
        }
    }

    private fun parseLocations(raw: String): List<Location> {
        if (raw.isBlank()) return emptyList()
        val array = JSONArray(raw)
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            Location(
                id = item.getString("id"),
                name = item.getString("name"),
                adminRegion = item.optString("adminRegion").ifBlank { null },
                country = item.getString("country"),
                latitude = item.getDouble("latitude"),
                longitude = item.getDouble("longitude"),
                timezone = item.getString("timezone"),
            )
        }
    }

    private fun serializeLocations(locations: List<Location>): String {
        val array = JSONArray()
        locations.forEach { location ->
            array.put(
                JSONObject().apply {
                    put("id", location.id)
                    put("name", location.name)
                    put("adminRegion", location.adminRegion ?: "")
                    put("country", location.country)
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                    put("timezone", location.timezone)
                },
            )
        }
        return array.toString()
    }

    private companion object {
        val FAVORITES_KEY = stringPreferencesKey("favorite_locations")
    }
}

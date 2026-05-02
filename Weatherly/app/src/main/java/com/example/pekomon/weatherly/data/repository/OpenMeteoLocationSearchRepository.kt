package com.example.pekomon.weatherly.data.repository

import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.data.remote.OpenMeteoGeocodingApi
import com.example.pekomon.weatherly.domain.repository.LocationSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale

class OpenMeteoLocationSearchRepository(
    private val api: OpenMeteoGeocodingApi = OpenMeteoGeocodingApi(),
) : LocationSearchRepository {
    override suspend fun searchLocations(query: String): List<Location> = withContext(Dispatchers.IO) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.length < 2) {
            return@withContext emptyList()
        }

        val payload = api.searchLocations(normalizedQuery)
        val root = JSONObject(payload)
        val results = root.optJSONArray("results") ?: return@withContext emptyList()

        List(results.length()) { index ->
            val item = results.getJSONObject(index)
            val countryCode = item.optString("country_code")
            Location(
                id = item.get("id").toString(),
                name = item.getString("name"),
                adminRegion = item.optString("admin1").ifBlank { null },
                country = item.optString("country").ifBlank {
                    countryCode.takeIf(String::isNotBlank)
                        ?.let {
                            Locale.Builder()
                                .setRegion(it)
                                .build()
                                .displayCountry
                        }
                        .orEmpty()
                },
                latitude = item.getDouble("latitude"),
                longitude = item.getDouble("longitude"),
                timezone = item.getString("timezone"),
            )
        }
    }
}

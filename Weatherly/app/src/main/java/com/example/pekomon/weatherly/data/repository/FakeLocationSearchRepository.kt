package com.example.pekomon.weatherly.data.repository

import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.domain.repository.LocationSearchRepository
import kotlinx.coroutines.delay

class FakeLocationSearchRepository : LocationSearchRepository {
    override suspend fun searchLocations(query: String): List<Location> {
        delay(250)

        if (query.isBlank()) {
            return emptyList()
        }

        return sampleLocations.filter { location ->
            val haystack = buildString {
                append(location.name)
                location.adminRegion?.let {
                    append(" ")
                    append(it)
                }
                append(" ")
                append(location.country)
            }

            haystack.contains(query.trim(), ignoreCase = true)
        }
    }
}

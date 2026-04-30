package com.example.pekomon.weatherly.domain.repository

import com.example.pekomon.weatherly.core.model.Location

interface LocationSearchRepository {
    suspend fun searchLocations(query: String): List<Location>
}

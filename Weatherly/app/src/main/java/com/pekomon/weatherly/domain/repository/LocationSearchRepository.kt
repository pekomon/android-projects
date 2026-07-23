package com.pekomon.weatherly.domain.repository

import com.pekomon.weatherly.core.model.Location

interface LocationSearchRepository {
    suspend fun searchLocations(query: String): List<Location>
}

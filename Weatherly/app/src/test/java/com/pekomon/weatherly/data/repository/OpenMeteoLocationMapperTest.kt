package com.pekomon.weatherly.data.repository

import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoLocationMapperTest {
    private val mapper = OpenMeteoLocationMapper()

    @Test
    fun mapsLocationsWithRegionAndCountryFallbacks() {
        val locations = mapper.map(resourceText("openmeteo/geocoding.json"))

        assertEquals(2, locations.size)
        assertEquals("658225", locations[0].id)
        assertEquals("Helsinki", locations[0].name)
        assertEquals("Uusimaa", locations[0].adminRegion)
        assertEquals("Finland", locations[0].country)
        assertEquals(60.1695, locations[0].latitude, 0.0001)
        assertEquals(24.9354, locations[0].longitude, 0.0001)
        assertEquals("Europe/Helsinki", locations[0].timezone)

        assertEquals("london-test", locations[1].id)
        assertEquals(null, locations[1].adminRegion)
        assertEquals("United Kingdom", locations[1].country)
    }

    @Test
    fun mapsMissingResultsToEmptyList() {
        assertTrue(mapper.map("{}").isEmpty())
    }

    @Test(expected = JSONException::class)
    fun malformedRequiredDataThrows() {
        mapper.map("""{"results":[{"id":1,"name":"Broken"}]}""")
    }
}

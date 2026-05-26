package com.pekomon.cryptoapp.data

import com.pekomon.cryptoapp.domain.market.MarketDataResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketPriceResponseMapperTest {
    @Test
    fun mapsCompletePriceResponseToSuccess() {
        val result = MarketPriceResponseMapper.mapPrices(
            requestedIds = listOf("bitcoin", "ethereum"),
            currency = "eur",
            response = mapOf(
                "bitcoin" to mapOf("eur" to 60_000.0),
                "ethereum" to mapOf("eur" to 3_000.0)
            )
        )

        assertTrue(result is MarketDataResult.Success)
        assertEquals(
            mapOf("bitcoin" to 60_000.0, "ethereum" to 3_000.0),
            (result as MarketDataResult.Success).value
        )
    }

    @Test
    fun mapsMissingIdsAndQuotesToPartialSuccess() {
        val result = MarketPriceResponseMapper.mapPrices(
            requestedIds = listOf("bitcoin", "ethereum", "solana"),
            currency = "eur",
            response = mapOf(
                "bitcoin" to mapOf("eur" to 60_000.0),
                "ethereum" to mapOf("usd" to 3_200.0)
            )
        )

        assertTrue(result is MarketDataResult.PartialSuccess)
        val partial = result as MarketDataResult.PartialSuccess
        assertEquals(mapOf("bitcoin" to 60_000.0), partial.value)
        assertEquals(setOf("ethereum", "solana"), partial.missingIds)
    }
}

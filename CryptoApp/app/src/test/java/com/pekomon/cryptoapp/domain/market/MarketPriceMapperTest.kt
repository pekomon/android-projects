package com.pekomon.cryptoapp.domain.market

import org.junit.Assert.assertEquals
import org.junit.Test

class MarketPriceMapperTest {
    @Test
    fun mapsRawPricesToDomainPrices() {
        val prices = MarketPriceMapper.mapPrices(
            mapOf(
                "bitcoin" to 60_000.0,
                "ethereum" to 3_000.0
            )
        )

        assertEquals(60_000.0, prices.getValue("bitcoin").currentPrice, 0.0)
        assertEquals("bitcoin", prices.getValue("bitcoin").cryptoId)
        assertEquals(0.0, prices.getValue("bitcoin").priceChangePercentage, 0.0)
        assertEquals(3_000.0, prices.getValue("ethereum").currentPrice, 0.0)
    }
}

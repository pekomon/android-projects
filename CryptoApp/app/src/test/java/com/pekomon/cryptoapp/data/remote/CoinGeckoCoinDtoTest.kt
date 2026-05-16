package com.pekomon.cryptoapp.data.remote

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class CoinGeckoCoinDtoTest {
    @Test
    fun gsonMapsMarketCapRankFromSnakeCase() {
        val json = """
            {
              "id": "bitcoin",
              "symbol": "btc",
              "name": "Bitcoin",
              "market_cap_rank": 1
            }
        """.trimIndent()

        val dto = Gson().fromJson(json, CoinGeckoCoinDto::class.java)

        assertEquals(1, dto.marketCapRank)
    }

    @Test
    fun toDomainMapsCoinFields() {
        val domain = CoinGeckoCoinDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            marketCapRank = 1
        ).toDomain()

        assertEquals("bitcoin", domain.id)
        assertEquals("btc", domain.symbol)
        assertEquals("Bitcoin", domain.name)
        assertEquals(1, domain.marketCapRank)
    }
}

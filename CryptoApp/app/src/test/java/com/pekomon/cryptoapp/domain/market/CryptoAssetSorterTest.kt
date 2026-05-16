package com.pekomon.cryptoapp.domain.market

import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.model.MarketPrice
import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoAssetSorterTest {
    @Test
    fun sortOrdersAssetsByNameAscending() {
        val sorted = CryptoAssetSorter.sort(
            assets = listOf(asset("bitcoin", "BTC", "Bitcoin"), asset("cardano", "ADA", "Cardano")),
            sortOption = SortOption.NAME_DESC,
            priceForAsset = { null }
        )

        assertEquals(listOf("cardano", "bitcoin"), sorted.map { it.id })
    }

    @Test
    fun sortOrdersAssetsByPriceDescending() {
        val sorted = CryptoAssetSorter.sort(
            assets = listOf(asset("bitcoin", "BTC", "Bitcoin"), asset("ethereum", "ETH", "Ethereum")),
            sortOption = SortOption.PRICE_DESC,
            priceForAsset = { id ->
                when (id) {
                    "bitcoin" -> MarketPrice(id, currentPrice = 100.0, priceChangePercentage = 0.0)
                    "ethereum" -> MarketPrice(id, currentPrice = 200.0, priceChangePercentage = 0.0)
                    else -> null
                }
            }
        )

        assertEquals(listOf("ethereum", "bitcoin"), sorted.map { it.id })
    }

    private fun asset(
        id: String,
        symbol: String,
        name: String
    ): CryptoAsset {
        return CryptoAsset(
            id = id,
            symbol = symbol,
            name = name,
            marketCapRank = null
        )
    }
}

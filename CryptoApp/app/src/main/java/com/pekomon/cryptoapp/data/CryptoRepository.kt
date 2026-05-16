package com.pekomon.cryptoapp.data

import com.pekomon.cryptoapp.data.remote.CoinGeckoApi
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.repository.MarketRepository

class CryptoRepository : MarketRepository {
    private val api = CoinGeckoApi.create()
    
    override suspend fun getAllAvailableCryptos(): List<CryptoAsset> {
        return api.getCoinsList().map { it.toDomain() }
    }
    
    override suspend fun getCryptoPrices(coinIds: List<String>, currency: String): Map<String, Double> {
        if (coinIds.isEmpty()) {
            return emptyMap()
        }

        val response = api.getSimplePrices(
            ids = coinIds.distinct().joinToString(","),
            vsCurrencies = currency
        )

        return response.mapValues { (_, prices) ->
            prices[currency] ?: error("Missing $currency price in CoinGecko response")
        }
    }
}

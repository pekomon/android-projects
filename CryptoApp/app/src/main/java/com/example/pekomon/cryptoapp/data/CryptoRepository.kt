package com.example.pekomon.cryptoapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CryptoRepository {
    private val api = CoinGeckoApi.create()
    
    suspend fun getAllAvailableCryptos(): List<CryptoListItem> {
        return api.getCoinsList()
    }
    
    suspend fun getCryptoPrices(coinIds: List<String>, currency: String): Map<String, Double> {
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

data class CryptoListItem(
    val id: String,
    val symbol: String,
    val name: String,
    val marketCapRank: Int?
)

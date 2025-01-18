package com.example.pekomon.cryptoapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CryptoRepository {
    private val api = CoinGeckoApi.create()
    
    suspend fun getAllAvailableCryptos(): List<CryptoListItem> {
        return api.getCoinsList()
    }
    
    suspend fun getCryptoPrices(coinIds: List<String>, currency: String): Map<String, Double> {
        return try {
            val response = api.getSimplePrices(
                ids = coinIds.joinToString(","),
                vsCurrencies = currency
            )
            response.mapValues { (_, prices) -> 
                prices[currency] ?: 0.0 
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

data class CryptoListItem(
    val id: String,
    val symbol: String,
    val name: String,
    val marketCapRank: Int?
) 
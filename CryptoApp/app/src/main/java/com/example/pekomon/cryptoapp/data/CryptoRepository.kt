package com.example.pekomon.cryptoapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CryptoRepository {
    private val api = Retrofit.Builder()
        .baseUrl("https://api.coingecko.com/api/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CoinGeckoApi::class.java)

    suspend fun getCryptoPrices(): Map<String, Double> {
        val response = api.getPrice(
            ids = "bitcoin,ethereum,dogecoin",
            vsCurrencies = "eur"
        )
        return response.mapValues { it.value["eur"] ?: 0.0 }
    }
} 
package com.example.pekomon.cryptoapp.data

import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("simple/price")
    suspend fun getPrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String = "eur"
    ): Map<String, Map<String, Double>>
} 
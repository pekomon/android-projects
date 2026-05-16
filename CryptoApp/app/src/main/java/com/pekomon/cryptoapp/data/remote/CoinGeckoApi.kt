package com.pekomon.cryptoapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("api/v3/coins/list")
    suspend fun getCoinsList(): List<CoinGeckoCoinDto>

    @GET("api/v3/simple/price")
    suspend fun getSimplePrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String
    ): Map<String, Map<String, Double>>

    companion object {
        private const val BASE_URL = "https://api.coingecko.com/"

        fun create(): CoinGeckoApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CoinGeckoApi::class.java)
        }
    }
}

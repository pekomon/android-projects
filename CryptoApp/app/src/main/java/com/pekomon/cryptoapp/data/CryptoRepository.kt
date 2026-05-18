package com.pekomon.cryptoapp.data

import android.util.Log
import com.pekomon.cryptoapp.BuildConfig
import com.pekomon.cryptoapp.data.remote.CoinGeckoApi
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.repository.MarketRepository
import retrofit2.HttpException

class CryptoRepository(
    private val coinGeckoDemoApiKey: String? = BuildConfig.COINGECKO_DEMO_API_KEY.takeIf { it.isNotBlank() }
) : MarketRepository {
    private val api = CoinGeckoApi.create()
    
    override suspend fun getAllAvailableCryptos(): List<CryptoAsset> {
        Log.d(TAG, "GET /coins/list keyConfigured=${coinGeckoDemoApiKey != null}")
        return try {
            val coins = api.getCoinsList(apiKey = coinGeckoDemoApiKey).map { it.toDomain() }
            Log.d(TAG, "GET /coins/list success count=${coins.size}")
            coins
        } catch (error: Exception) {
            Log.e(TAG, "GET /coins/list failed ${error.debugSummary()}", error)
            throw error
        }
    }
    
    override suspend fun getCryptoPrices(coinIds: List<String>, currency: String): Map<String, Double> {
        if (coinIds.isEmpty()) {
            Log.d(TAG, "GET /simple/price skipped: empty coinIds")
            return emptyMap()
        }

        val distinctCoinIds = coinIds.distinct()
        Log.d(
            TAG,
            "GET /simple/price ids=${distinctCoinIds.joinToString(",")} currency=$currency keyConfigured=${coinGeckoDemoApiKey != null}"
        )

        val response = try {
            api.getSimplePrices(
                ids = distinctCoinIds.joinToString(","),
                vsCurrencies = currency,
                apiKey = coinGeckoDemoApiKey
            )
        } catch (error: Exception) {
            Log.e(TAG, "GET /simple/price failed ${error.debugSummary()}", error)
            throw error
        }

        Log.d(TAG, "GET /simple/price success returnedIds=${response.keys.joinToString(",")}")

        val missingPriceIds = response
            .filterValues { prices -> prices[currency] == null }
            .keys

        if (missingPriceIds.isNotEmpty()) {
            Log.w(TAG, "GET /simple/price missing $currency prices for ids=${missingPriceIds.joinToString(",")}")
        }

        return response.mapNotNull { (id, prices) ->
            prices[currency]?.let { price -> id to price }
        }.toMap()
    }

    private fun Exception.debugSummary(): String {
        return if (this is HttpException) {
            val errorBody = response()?.errorBody()?.string()?.take(500)
            "httpCode=${code()} message=${message()} errorBody=$errorBody"
        } else {
            "${this::class.java.simpleName}: ${message}"
        }
    }

    private companion object {
        const val TAG = "CryptoAppNetwork"
    }
}

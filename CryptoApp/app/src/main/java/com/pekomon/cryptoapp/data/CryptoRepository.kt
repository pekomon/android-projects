package com.pekomon.cryptoapp.data

import com.pekomon.cryptoapp.BuildConfig
import com.pekomon.cryptoapp.core.logging.CryptoAppLogger
import com.pekomon.cryptoapp.data.remote.CoinGeckoApi
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.repository.MarketRepository
import retrofit2.HttpException

class CryptoRepository(
    private val coinGeckoDemoApiKey: String? = BuildConfig.COINGECKO_DEMO_API_KEY.takeIf { it.isNotBlank() }
) : MarketRepository {
    private val api = CoinGeckoApi.create()
    
    override suspend fun getAllAvailableCryptos(): List<CryptoAsset> {
        CryptoAppLogger.debug(TAG, "GET /coins/list keyConfigured=${coinGeckoDemoApiKey != null}")
        return try {
            val coins = api.getCoinsList(apiKey = coinGeckoDemoApiKey).map { it.toDomain() }
            CryptoAppLogger.debug(TAG, "GET /coins/list success count=${coins.size}")
            coins
        } catch (error: Exception) {
            CryptoAppLogger.error(TAG, "GET /coins/list failed ${error.debugSummary()}", error)
            throw error
        }
    }
    
    override suspend fun getCryptoPrices(coinIds: List<String>, currency: String): Map<String, Double> {
        if (coinIds.isEmpty()) {
            CryptoAppLogger.debug(TAG, "GET /simple/price skipped: empty coinIds")
            return emptyMap()
        }

        val distinctCoinIds = coinIds.distinct()
        CryptoAppLogger.debug(
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
            CryptoAppLogger.error(TAG, "GET /simple/price failed ${error.debugSummary()}", error)
            throw error
        }

        CryptoAppLogger.debug(TAG, "GET /simple/price success returnedIds=${response.keys.joinToString(",")}")

        val missingPriceIds = response
            .filterValues { prices -> prices[currency] == null }
            .keys

        if (missingPriceIds.isNotEmpty()) {
            CryptoAppLogger.warning(TAG, "GET /simple/price missing $currency prices for ids=${missingPriceIds.joinToString(",")}")
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

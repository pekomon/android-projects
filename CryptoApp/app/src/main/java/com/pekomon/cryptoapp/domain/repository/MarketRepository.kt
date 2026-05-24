package com.pekomon.cryptoapp.domain.repository

import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.market.MarketDataResult

interface MarketRepository {
    suspend fun getAllAvailableCryptos(): List<CryptoAsset>

    suspend fun getCryptoPrices(
        coinIds: List<String>,
        currency: String
    ): Map<String, Double>

    suspend fun getCryptoPricesResult(
        coinIds: List<String>,
        currency: String
    ): MarketDataResult<Map<String, Double>>
}

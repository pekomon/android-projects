package com.pekomon.cryptoapp.domain.repository

import com.pekomon.cryptoapp.domain.model.CryptoAsset

interface MarketRepository {
    suspend fun getAllAvailableCryptos(): List<CryptoAsset>

    suspend fun getCryptoPrices(
        coinIds: List<String>,
        currency: String
    ): Map<String, Double>
}

package com.pekomon.cryptoapp.data.remote

import com.pekomon.cryptoapp.domain.model.CryptoAsset

data class CoinGeckoCoinDto(
    val id: String,
    val symbol: String,
    val name: String,
    val marketCapRank: Int?
) {
    fun toDomain(): CryptoAsset {
        return CryptoAsset(
            id = id,
            symbol = symbol,
            name = name,
            marketCapRank = marketCapRank
        )
    }
}

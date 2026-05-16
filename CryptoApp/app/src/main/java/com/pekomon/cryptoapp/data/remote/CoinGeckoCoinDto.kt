package com.pekomon.cryptoapp.data.remote

import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.google.gson.annotations.SerializedName

data class CoinGeckoCoinDto(
    val id: String,
    val symbol: String,
    val name: String,
    @SerializedName("market_cap_rank")
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

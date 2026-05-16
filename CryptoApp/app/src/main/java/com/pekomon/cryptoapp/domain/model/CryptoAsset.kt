package com.pekomon.cryptoapp.domain.model

data class CryptoAsset(
    val id: String,
    val symbol: String,
    val name: String,
    val marketCapRank: Int?
)

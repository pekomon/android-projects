package com.pekomon.cryptoapp.domain.model

data class MarketPrice(
    val cryptoId: String,
    val currentPrice: Double,
    val priceChangePercentage: Double
)

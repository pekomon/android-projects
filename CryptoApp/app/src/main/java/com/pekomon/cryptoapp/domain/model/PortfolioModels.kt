package com.pekomon.cryptoapp.domain.model

import java.time.LocalDateTime

data class PortfolioPosition(
    val cryptoId: String,
    val amount: Double,
    val averageCost: Double,
    val firstPurchasedAt: LocalDateTime,
    val transactions: List<PortfolioTransaction>
)

data class PortfolioTransaction(
    val type: PortfolioTransactionType,
    val amount: Double,
    val price: Double,
    val dateTime: LocalDateTime
) {
    val signedAmount: Double
        get() = when (type) {
            PortfolioTransactionType.BUY -> amount
            PortfolioTransactionType.SELL -> -amount
        }
}

enum class PortfolioTransactionType {
    BUY,
    SELL
}

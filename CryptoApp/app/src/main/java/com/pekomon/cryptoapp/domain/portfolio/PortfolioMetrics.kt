package com.pekomon.cryptoapp.domain.portfolio

data class PortfolioHoldingMetrics(
    val cryptoId: String,
    val amount: Double,
    val averageCost: Double,
    val costBasis: Double,
    val currentPrice: Double?,
    val currentValue: Double?,
    val profitLoss: Double?,
    val profitLossPercentage: Double?,
    val allocationPercentage: Double
)

data class PortfolioSummaryMetrics(
    val holdingCount: Int,
    val pricedHoldingCount: Int,
    val investedValue: Double,
    val currentValue: Double,
    val profitLoss: Double,
    val profitLossPercentage: Double
) {
    val unpricedHoldingCount: Int = holdingCount - pricedHoldingCount
}

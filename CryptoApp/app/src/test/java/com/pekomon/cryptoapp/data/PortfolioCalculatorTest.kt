package com.pekomon.cryptoapp.data

import com.pekomon.cryptoapp.domain.portfolio.PortfolioCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class PortfolioCalculatorTest {
    @Test
    fun combineHoldingsAggregatesAmountAndWeightedPurchasePrice() {
        val firstDate = LocalDateTime.of(2026, 1, 1, 10, 0)
        val secondDate = LocalDateTime.of(2026, 1, 2, 10, 0)
        val holdings = listOf(
            holding("bitcoin", amount = 1.0, purchasePrice = 100.0, dateTime = firstDate),
            holding("bitcoin", amount = 3.0, purchasePrice = 200.0, dateTime = secondDate),
            holding("ethereum", amount = 2.0, purchasePrice = 50.0, dateTime = secondDate)
        )

        val combined = PortfolioCalculator.combineHoldings(holdings)
        val bitcoin = combined.first { it.cryptoId == "bitcoin" }

        assertEquals(2, combined.size)
        assertEquals(4.0, bitcoin.amount, 0.0)
        assertEquals(175.0, bitcoin.purchasePrice, 0.0)
        assertEquals(firstDate, bitcoin.purchaseDateTime)
        assertEquals(2, bitcoin.transactions.size)
    }

    @Test
    fun totalValueUsesCombinedHoldingsAndMissingPricesAsZero() {
        val holdings = listOf(
            holding("bitcoin", amount = 1.0, purchasePrice = 100.0),
            holding("bitcoin", amount = 2.0, purchasePrice = 150.0),
            holding("ethereum", amount = 4.0, purchasePrice = 50.0)
        )

        val totalValue = PortfolioCalculator.totalValue(holdings) { cryptoId ->
            when (cryptoId) {
                "bitcoin" -> 200.0
                else -> null
            }
        }

        assertEquals(600.0, totalValue, 0.0)
    }

    @Test
    fun combineHoldingsDerivesAmountFromTransactions() {
        val dateTime = LocalDateTime.of(2026, 1, 1, 10, 0)
        val holdings = listOf(
            UserCrypto(
                cryptoId = "bitcoin",
                amount = 99.0,
                purchasePrice = 999.0,
                purchaseDateTime = dateTime,
                transactions = listOf(
                    Transaction(
                        type = TransactionType.BUY,
                        amount = 3.0,
                        price = 100.0,
                        dateTime = dateTime
                    ),
                    Transaction(
                        type = TransactionType.SELL,
                        amount = 1.0,
                        price = 150.0,
                        dateTime = dateTime.plusDays(1)
                    )
                )
            )
        )

        val combined = PortfolioCalculator.combineHoldings(holdings)

        assertEquals(1, combined.size)
        assertEquals(2.0, combined.first().amount, 0.0)
        assertEquals(100.0, combined.first().purchasePrice, 0.0)
    }

    @Test
    fun holdingMetricsDeriveValueGainLossAndAllocation() {
        val holdings = listOf(
            holding("bitcoin", amount = 2.0, purchasePrice = 100.0),
            holding("ethereum", amount = 1.0, purchasePrice = 50.0)
        )

        val metrics = PortfolioCalculator.holdingMetrics(holdings) { cryptoId ->
            when (cryptoId) {
                "bitcoin" -> 150.0
                "ethereum" -> 100.0
                else -> null
            }
        }
        val bitcoin = metrics.first { it.cryptoId == "bitcoin" }

        assertEquals(200.0, bitcoin.costBasis, 0.0)
        assertEquals(300.0, bitcoin.currentValue ?: 0.0, 0.0)
        assertEquals(100.0, bitcoin.profitLoss ?: 0.0, 0.0)
        assertEquals(50.0, bitcoin.profitLossPercentage ?: 0.0, 0.0)
        assertEquals(75.0, bitcoin.allocationPercentage, 0.0)
    }

    @Test
    fun summaryMetricsExcludeUnpricedHoldingsFromProfitLoss() {
        val holdings = listOf(
            holding("bitcoin", amount = 2.0, purchasePrice = 100.0),
            holding("ethereum", amount = 1.0, purchasePrice = 50.0)
        )

        val metrics = PortfolioCalculator.summaryMetrics(holdings) { cryptoId ->
            when (cryptoId) {
                "bitcoin" -> 150.0
                else -> null
            }
        }

        assertEquals(2, metrics.holdingCount)
        assertEquals(1, metrics.pricedHoldingCount)
        assertEquals(250.0, metrics.investedValue, 0.0)
        assertEquals(300.0, metrics.currentValue, 0.0)
        assertEquals(100.0, metrics.profitLoss, 0.0)
        assertEquals(50.0, metrics.profitLossPercentage, 0.0)
        assertEquals(1, metrics.unpricedHoldingCount)
    }

    private fun holding(
        cryptoId: String,
        amount: Double,
        purchasePrice: Double,
        dateTime: LocalDateTime = LocalDateTime.of(2026, 1, 1, 10, 0)
    ): UserCrypto {
        return UserCrypto(
            cryptoId = cryptoId,
            amount = amount,
            purchasePrice = purchasePrice,
            purchaseDateTime = dateTime,
            transactions = listOf(
                Transaction(
                    type = TransactionType.BUY,
                    amount = amount,
                    price = purchasePrice,
                    dateTime = dateTime
                )
            )
        )
    }
}

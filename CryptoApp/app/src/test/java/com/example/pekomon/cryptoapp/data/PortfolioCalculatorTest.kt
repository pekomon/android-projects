package com.example.pekomon.cryptoapp.data

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

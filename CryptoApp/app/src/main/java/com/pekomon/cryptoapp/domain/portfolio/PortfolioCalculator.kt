package com.pekomon.cryptoapp.domain.portfolio

import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.data.Transaction
import com.pekomon.cryptoapp.data.TransactionType
import java.time.LocalDateTime

object PortfolioCalculator {
    fun combineHoldings(holdings: List<UserCrypto>): List<UserCrypto> {
        return holdings
            .groupBy { it.cryptoId }
            .mapNotNull { (cryptoId, cryptos) ->
                val transactions = cryptos.flatMap { it.effectiveTransactions() }
                val totalAmount = transactions.sumOf { it.signedAmount() }

                if (totalAmount <= 0.0) {
                    null
                } else {
                    UserCrypto(
                        cryptoId = cryptoId,
                        amount = totalAmount,
                        purchasePrice = transactions.averageBuyPrice(),
                        purchaseDateTime = transactions.minOfOrNull { it.dateTime } ?: LocalDateTime.now(),
                        transactions = transactions.sortedBy { it.dateTime }
                    )
                }
            }
    }

    fun normalizeHoldings(holdings: List<UserCrypto>): List<UserCrypto> {
        return holdings
            .groupBy { it.cryptoId }
            .map { (cryptoId, cryptos) ->
                val transactions = cryptos.flatMap { it.effectiveTransactions() }
                val totalAmount = transactions.sumOf { it.signedAmount() }
                UserCrypto(
                    cryptoId = cryptoId,
                    amount = totalAmount.coerceAtLeast(0.0),
                    purchasePrice = transactions.averageBuyPrice(),
                    purchaseDateTime = transactions.minOfOrNull { it.dateTime } ?: LocalDateTime.now(),
                    transactions = transactions.sortedBy { it.dateTime }
                )
            }
            .filter { it.transactions.isNotEmpty() && it.amount > 0.0 }
    }

    fun totalValue(
        holdings: List<UserCrypto>,
        priceForCrypto: (String) -> Double?
    ): Double {
        return combineHoldings(holdings).sumOf { holding ->
            (priceForCrypto(holding.cryptoId) ?: 0.0) * holding.amount
        }
    }

    fun holdingMetrics(
        holdings: List<UserCrypto>,
        priceForCrypto: (String) -> Double?
    ): List<PortfolioHoldingMetrics> {
        val combinedHoldings = combineHoldings(holdings)
        val currentValues = combinedHoldings.associate { holding ->
            holding.cryptoId to priceForCrypto(holding.cryptoId)?.let { price -> price * holding.amount }
        }
        val totalCurrentValue = currentValues.values.filterNotNull().sum()

        return combinedHoldings.map { holding ->
            val costBasis = holding.purchasePrice * holding.amount
            val currentPrice = priceForCrypto(holding.cryptoId)
            val currentValue = currentValues[holding.cryptoId]
            val profitLoss = currentValue?.let { it - costBasis }
            val profitLossPercentage = if (costBasis == 0.0 || profitLoss == null) {
                null
            } else {
                (profitLoss / costBasis) * 100
            }
            val allocationPercentage = if (currentValue == null || totalCurrentValue == 0.0) {
                0.0
            } else {
                (currentValue / totalCurrentValue) * 100
            }

            PortfolioHoldingMetrics(
                cryptoId = holding.cryptoId,
                amount = holding.amount,
                averageCost = holding.purchasePrice,
                costBasis = costBasis,
                currentPrice = currentPrice,
                currentValue = currentValue,
                profitLoss = profitLoss,
                profitLossPercentage = profitLossPercentage,
                allocationPercentage = allocationPercentage
            )
        }
    }

    fun summaryMetrics(
        holdings: List<UserCrypto>,
        priceForCrypto: (String) -> Double?
    ): PortfolioSummaryMetrics {
        val holdingMetrics = holdingMetrics(holdings, priceForCrypto)
        val pricedHoldings = holdingMetrics.filter { it.currentValue != null }
        val investedValue = holdingMetrics.sumOf { it.costBasis }
        val currentValue = pricedHoldings.sumOf { it.currentValue ?: 0.0 }
        val pricedInvestedValue = pricedHoldings.sumOf { it.costBasis }
        val profitLoss = currentValue - pricedInvestedValue
        val profitLossPercentage = if (pricedInvestedValue == 0.0) {
            0.0
        } else {
            (profitLoss / pricedInvestedValue) * 100
        }

        return PortfolioSummaryMetrics(
            holdingCount = holdingMetrics.size,
            pricedHoldingCount = pricedHoldings.size,
            investedValue = investedValue,
            currentValue = currentValue,
            profitLoss = profitLoss,
            profitLossPercentage = profitLossPercentage
        )
    }

    private fun UserCrypto.effectiveTransactions(): List<Transaction> {
        return transactions.ifEmpty {
            listOf(
                Transaction(
                    type = TransactionType.BUY,
                    amount = amount,
                    price = purchasePrice,
                    dateTime = purchaseDateTime
                )
            )
        }
    }

    private fun Transaction.signedAmount(): Double {
        return when (type) {
            TransactionType.BUY -> amount
            TransactionType.SELL -> -amount
        }
    }

    private fun List<Transaction>.averageBuyPrice(): Double {
        val buys = filter { it.type == TransactionType.BUY }
        val totalBought = buys.sumOf { it.amount }
        return if (totalBought == 0.0) {
            0.0
        } else {
            buys.sumOf { it.price * it.amount } / totalBought
        }
    }
}

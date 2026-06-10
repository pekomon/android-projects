package com.pekomon.cryptoapp.domain.portfolio

import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.data.Transaction
import com.pekomon.cryptoapp.data.TransactionType
import com.pekomon.cryptoapp.domain.model.PortfolioPosition
import com.pekomon.cryptoapp.domain.model.PortfolioTransaction
import com.pekomon.cryptoapp.domain.model.PortfolioTransactionType
import java.time.LocalDateTime

object PortfolioCalculator {
    fun combineHoldings(holdings: List<UserCrypto>): List<UserCrypto> {
        return combinePositions(holdings.map { it.toPortfolioPosition() }).map { it.toUserCrypto() }
    }

    fun combinePositions(positions: List<PortfolioPosition>): List<PortfolioPosition> {
        return positions
            .groupBy { it.cryptoId }
            .mapNotNull { (cryptoId, groupedPositions) ->
                val transactions = groupedPositions.flatMap { it.transactions }
                val totalAmount = transactions.sumOf { it.signedAmount }

                if (totalAmount <= 0.0) {
                    null
                } else {
                    PortfolioPosition(
                        cryptoId = cryptoId,
                        amount = totalAmount,
                        averageCost = transactions.averagePortfolioBuyPrice(),
                        firstPurchasedAt = transactions.minOfOrNull { it.dateTime } ?: LocalDateTime.now(),
                        transactions = transactions.sortedBy { it.dateTime }
                    )
                }
            }
    }

    fun normalizeHoldings(holdings: List<UserCrypto>): List<UserCrypto> {
        return normalizePositions(holdings.map { it.toPortfolioPosition() }).map { it.toUserCrypto() }
    }

    fun normalizePositions(positions: List<PortfolioPosition>): List<PortfolioPosition> {
        return positions
            .groupBy { it.cryptoId }
            .map { (cryptoId, groupedPositions) ->
                val transactions = groupedPositions.flatMap { it.transactions }
                val totalAmount = transactions.sumOf { it.signedAmount }
                PortfolioPosition(
                    cryptoId = cryptoId,
                    amount = totalAmount.coerceAtLeast(0.0),
                    averageCost = transactions.averagePortfolioBuyPrice(),
                    firstPurchasedAt = transactions.minOfOrNull { it.dateTime } ?: LocalDateTime.now(),
                    transactions = transactions.sortedBy { it.dateTime }
                )
            }
            .filter { it.transactions.isNotEmpty() && it.amount > 0.0 }
    }

    fun totalValue(
        holdings: List<UserCrypto>,
        priceForCrypto: (String) -> Double?
    ): Double {
        return totalPositionValue(holdings.map { it.toPortfolioPosition() }, priceForCrypto)
    }

    fun totalPositionValue(
        positions: List<PortfolioPosition>,
        priceForCrypto: (String) -> Double?
    ): Double {
        return combinePositions(positions).sumOf { holding ->
            (priceForCrypto(holding.cryptoId) ?: 0.0) * holding.amount
        }
    }

    fun holdingMetrics(
        holdings: List<UserCrypto>,
        priceForCrypto: (String) -> Double?
    ): List<PortfolioHoldingMetrics> {
        return positionMetrics(holdings.map { it.toPortfolioPosition() }, priceForCrypto)
    }

    fun positionMetrics(
        positions: List<PortfolioPosition>,
        priceForCrypto: (String) -> Double?
    ): List<PortfolioHoldingMetrics> {
        val combinedHoldings = combinePositions(positions)
        val currentValues = combinedHoldings.associate { holding ->
            holding.cryptoId to priceForCrypto(holding.cryptoId)?.let { price -> price * holding.amount }
        }
        val totalCurrentValue = currentValues.values.filterNotNull().sum()

        return combinedHoldings.map { holding ->
            val costBasis = holding.averageCost * holding.amount
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
                averageCost = holding.averageCost,
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
        return positionSummaryMetrics(holdings.map { it.toPortfolioPosition() }, priceForCrypto)
    }

    fun positionSummaryMetrics(
        positions: List<PortfolioPosition>,
        priceForCrypto: (String) -> Double?
    ): PortfolioSummaryMetrics {
        val holdingMetrics = positionMetrics(positions, priceForCrypto)
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

    private fun UserCrypto.toPortfolioPosition(): PortfolioPosition {
        return PortfolioPosition(
            cryptoId = cryptoId,
            amount = amount,
            averageCost = purchasePrice,
            firstPurchasedAt = purchaseDateTime,
            transactions = effectiveTransactions().map { it.toPortfolioTransaction() }
        )
    }

    private fun PortfolioPosition.toUserCrypto(): UserCrypto {
        return UserCrypto(
            cryptoId = cryptoId,
            amount = amount,
            purchasePrice = averageCost,
            purchaseDateTime = firstPurchasedAt,
            transactions = transactions.map { it.toDataTransaction() }
        )
    }

    private fun Transaction.toPortfolioTransaction(): PortfolioTransaction {
        return PortfolioTransaction(
            type = when (type) {
                TransactionType.BUY -> PortfolioTransactionType.BUY
                TransactionType.SELL -> PortfolioTransactionType.SELL
            },
            amount = amount,
            price = price,
            dateTime = dateTime
        )
    }

    private fun PortfolioTransaction.toDataTransaction(): Transaction {
        return Transaction(
            type = when (type) {
                PortfolioTransactionType.BUY -> TransactionType.BUY
                PortfolioTransactionType.SELL -> TransactionType.SELL
            },
            amount = amount,
            price = price,
            dateTime = dateTime
        )
    }

    /*
     * Legacy implementation kept below for reference during the migration to
     * PortfolioPosition-backed calculations.
     */
    private fun legacyCombineHoldings(holdings: List<UserCrypto>): List<UserCrypto> {
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

    private fun legacyNormalizeHoldings(holdings: List<UserCrypto>): List<UserCrypto> {
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

    private fun legacyTotalValue(
        holdings: List<UserCrypto>,
        priceForCrypto: (String) -> Double?
    ): Double {
        return combineHoldings(holdings).sumOf { holding ->
            (priceForCrypto(holding.cryptoId) ?: 0.0) * holding.amount
        }
    }

    private fun legacyHoldingMetrics(
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

    private fun legacySummaryMetrics(
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

    private fun List<PortfolioTransaction>.averagePortfolioBuyPrice(): Double {
        val buys = filter { it.type == PortfolioTransactionType.BUY }
        val totalBought = buys.sumOf { it.amount }
        return if (totalBought == 0.0) {
            0.0
        } else {
            buys.sumOf { it.price * it.amount } / totalBought
        }
    }
}

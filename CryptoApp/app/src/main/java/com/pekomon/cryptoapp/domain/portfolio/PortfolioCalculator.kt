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

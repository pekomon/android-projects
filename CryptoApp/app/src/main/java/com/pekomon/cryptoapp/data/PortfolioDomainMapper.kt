package com.pekomon.cryptoapp.data

import com.pekomon.cryptoapp.domain.model.PortfolioPosition
import com.pekomon.cryptoapp.domain.model.PortfolioTransaction
import com.pekomon.cryptoapp.domain.model.PortfolioTransactionType

fun UserCrypto.toPortfolioPosition(): PortfolioPosition {
    val effectiveTransactions = transactions.ifEmpty {
        listOf(
            Transaction(
                type = TransactionType.BUY,
                amount = amount,
                price = purchasePrice,
                dateTime = purchaseDateTime
            )
        )
    }

    return PortfolioPosition(
        cryptoId = cryptoId,
        amount = amount,
        averageCost = purchasePrice,
        firstPurchasedAt = purchaseDateTime,
        transactions = effectiveTransactions.map { it.toPortfolioTransaction() }
    )
}

fun PortfolioPosition.toUserCrypto(): UserCrypto {
    return UserCrypto(
        cryptoId = cryptoId,
        amount = amount,
        purchasePrice = averageCost,
        purchaseDateTime = firstPurchasedAt,
        transactions = transactions.map { it.toPersistedTransaction() }
    )
}

fun Transaction.toPortfolioTransaction(): PortfolioTransaction {
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

fun PortfolioTransaction.toPersistedTransaction(): Transaction {
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

package com.pekomon.cryptoapp.data

import com.pekomon.cryptoapp.domain.model.PortfolioPosition
import com.pekomon.cryptoapp.domain.model.PortfolioTransaction
import com.pekomon.cryptoapp.domain.model.PortfolioTransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class PortfolioDomainMapperTest {
    @Test
    fun userCryptoMapsTransactionsIntoDomainPosition() {
        val firstDate = LocalDateTime.of(2026, 1, 1, 10, 0)
        val secondDate = LocalDateTime.of(2026, 1, 2, 10, 0)
        val userCrypto = UserCrypto(
            cryptoId = "bitcoin",
            amount = 1.5,
            purchasePrice = 100.0,
            purchaseDateTime = firstDate,
            transactions = listOf(
                Transaction(TransactionType.BUY, amount = 2.0, price = 100.0, dateTime = firstDate),
                Transaction(TransactionType.SELL, amount = 0.5, price = 120.0, dateTime = secondDate)
            )
        )

        val position = userCrypto.toPortfolioPosition()

        assertEquals("bitcoin", position.cryptoId)
        assertEquals(1.5, position.amount, 0.0)
        assertEquals(100.0, position.averageCost, 0.0)
        assertEquals(firstDate, position.firstPurchasedAt)
        assertEquals(PortfolioTransactionType.BUY, position.transactions[0].type)
        assertEquals(PortfolioTransactionType.SELL, position.transactions[1].type)
    }

    @Test
    fun userCryptoWithoutTransactionsMapsLegacyAggregateAsBuyTransaction() {
        val dateTime = LocalDateTime.of(2026, 1, 1, 10, 0)
        val userCrypto = UserCrypto(
            cryptoId = "ethereum",
            amount = 3.0,
            purchasePrice = 50.0,
            purchaseDateTime = dateTime,
            transactions = emptyList()
        )

        val transaction = userCrypto.toPortfolioPosition().transactions.single()

        assertEquals(PortfolioTransactionType.BUY, transaction.type)
        assertEquals(3.0, transaction.amount, 0.0)
        assertEquals(50.0, transaction.price, 0.0)
        assertEquals(dateTime, transaction.dateTime)
    }

    @Test
    fun domainPositionMapsBackToPersistedUserCrypto() {
        val dateTime = LocalDateTime.of(2026, 1, 1, 10, 0)
        val position = PortfolioPosition(
            cryptoId = "solana",
            amount = 4.0,
            averageCost = 25.0,
            firstPurchasedAt = dateTime,
            transactions = listOf(
                PortfolioTransaction(
                    type = PortfolioTransactionType.BUY,
                    amount = 4.0,
                    price = 25.0,
                    dateTime = dateTime
                )
            )
        )

        val userCrypto = position.toUserCrypto()

        assertEquals("solana", userCrypto.cryptoId)
        assertEquals(4.0, userCrypto.amount, 0.0)
        assertEquals(25.0, userCrypto.purchasePrice, 0.0)
        assertEquals(dateTime, userCrypto.purchaseDateTime)
        assertEquals(TransactionType.BUY, userCrypto.transactions.single().type)
    }
}

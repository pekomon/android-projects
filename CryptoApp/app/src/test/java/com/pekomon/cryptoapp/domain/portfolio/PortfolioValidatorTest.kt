package com.pekomon.cryptoapp.domain.portfolio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortfolioValidatorTest {
    @Test
    fun validateTransactionInputAcceptsPositiveAmountAndPrice() {
        val result = PortfolioValidator.validateTransactionInput(amount = 1.2, price = 250.0)

        assertEquals(PortfolioValidationResult.Valid, result)
    }

    @Test
    fun validateTransactionInputRejectsZeroAmount() {
        val result = PortfolioValidator.validateTransactionInput(amount = 0.0, price = 250.0)

        assertTrue(result is PortfolioValidationResult.Invalid)
    }

    @Test
    fun validateTransactionInputRejectsZeroPrice() {
        val result = PortfolioValidator.validateTransactionInput(amount = 1.2, price = 0.0)

        assertTrue(result is PortfolioValidationResult.Invalid)
    }
}

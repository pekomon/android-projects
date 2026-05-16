package com.pekomon.cryptoapp.domain.portfolio

object PortfolioValidator {
    fun validateTransactionInput(
        amount: Double,
        price: Double
    ): PortfolioValidationResult {
        return when {
            amount <= 0.0 -> PortfolioValidationResult.Invalid("Amount must be greater than zero.")
            price <= 0.0 -> PortfolioValidationResult.Invalid("Price must be greater than zero.")
            else -> PortfolioValidationResult.Valid
        }
    }
}

sealed interface PortfolioValidationResult {
    data object Valid : PortfolioValidationResult

    data class Invalid(
        val message: String
    ) : PortfolioValidationResult
}

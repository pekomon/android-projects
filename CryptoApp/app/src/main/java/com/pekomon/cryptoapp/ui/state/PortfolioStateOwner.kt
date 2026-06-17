package com.pekomon.cryptoapp.ui.state

import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.Transaction
import com.pekomon.cryptoapp.data.TransactionType
import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.data.toPortfolioPosition
import com.pekomon.cryptoapp.domain.model.MarketPrice
import com.pekomon.cryptoapp.domain.model.PortfolioPosition
import com.pekomon.cryptoapp.domain.portfolio.PortfolioCalculator
import com.pekomon.cryptoapp.domain.portfolio.PortfolioValidationResult
import com.pekomon.cryptoapp.domain.portfolio.PortfolioValidator
import com.pekomon.cryptoapp.ui.MarketLoadState
import java.time.LocalDateTime

class PortfolioStateOwner {
    fun positions(userCryptos: List<UserCrypto>): List<PortfolioPosition> {
        return PortfolioCalculator.combinePositions(
            userCryptos.map { it.toPortfolioPosition() }
        )
    }

    fun state(
        userCryptos: List<UserCrypto>,
        prices: Map<String, MarketPrice>,
        selectedCurrency: Currency,
        marketLoadState: MarketLoadState,
        isLoading: Boolean,
        errorMessage: String?
    ): PortfolioUiState {
        val positions = positions(userCryptos)
        return PortfolioUiState(
            positions = positions,
            summary = PortfolioCalculator.positionSummaryMetrics(positions) { cryptoId ->
                prices[cryptoId]?.currentPrice
            },
            holdingMetrics = PortfolioCalculator.positionMetrics(positions) { cryptoId ->
                prices[cryptoId]?.currentPrice
            }.associateBy { it.cryptoId },
            selectedCurrency = selectedCurrency,
            marketLoadState = marketLoadState,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }

    fun addHolding(
        currentHoldings: List<UserCrypto>,
        cryptoId: String,
        amount: Double,
        price: Double,
        dateTime: LocalDateTime
    ): PortfolioMutationResult {
        validate(amount, price)?.let { return it }

        val newHolding = UserCrypto(
            cryptoId = cryptoId,
            amount = amount,
            purchasePrice = price,
            purchaseDateTime = dateTime,
            transactions = listOf(
                Transaction(
                    type = TransactionType.BUY,
                    amount = amount,
                    price = price,
                    dateTime = dateTime
                )
            )
        )

        return PortfolioMutationResult.Success(
            holdings = PortfolioCalculator.normalizeHoldings(currentHoldings + newHolding)
        )
    }

    fun updateHolding(
        currentHoldings: List<UserCrypto>,
        cryptoId: String,
        amount: Double,
        price: Double,
        dateTime: LocalDateTime
    ): PortfolioMutationResult {
        validate(amount, price)?.let { return it }

        val existingHolding = PortfolioCalculator.combineHoldings(currentHoldings)
            .find { it.cryptoId == cryptoId }
            ?: return PortfolioMutationResult.Failure("This holding is no longer in your portfolio.")

        val adjustment = amount - existingHolding.amount
        val adjustmentTransaction = when {
            adjustment > 0.0 -> Transaction(
                type = TransactionType.BUY,
                amount = adjustment,
                price = price,
                dateTime = dateTime
            )
            adjustment < 0.0 -> Transaction(
                type = TransactionType.SELL,
                amount = -adjustment,
                price = price,
                dateTime = dateTime
            )
            else -> return PortfolioMutationResult.NoChange
        }

        val updatedHolding = existingHolding.copy(
            transactions = existingHolding.transactions + adjustmentTransaction
        )

        return PortfolioMutationResult.Success(
            holdings = PortfolioCalculator.normalizeHoldings(
                currentHoldings.filterNot { it.cryptoId == cryptoId } + updatedHolding
            )
        )
    }

    fun removeHolding(
        currentHoldings: List<UserCrypto>,
        cryptoId: String
    ): List<UserCrypto> {
        return currentHoldings.filter { it.cryptoId != cryptoId }
    }

    private fun validate(
        amount: Double,
        price: Double
    ): PortfolioMutationResult.Failure? {
        val validation = PortfolioValidator.validateTransactionInput(amount, price)
        return if (validation is PortfolioValidationResult.Invalid) {
            PortfolioMutationResult.Failure(validation.message)
        } else {
            null
        }
    }
}

sealed interface PortfolioMutationResult {
    data class Success(
        val holdings: List<UserCrypto>
    ) : PortfolioMutationResult

    data class Failure(
        val message: String
    ) : PortfolioMutationResult

    data object NoChange : PortfolioMutationResult
}

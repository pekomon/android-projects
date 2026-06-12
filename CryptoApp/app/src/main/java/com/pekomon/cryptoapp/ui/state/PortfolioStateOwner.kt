package com.pekomon.cryptoapp.ui.state

import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.data.toPortfolioPosition
import com.pekomon.cryptoapp.domain.model.MarketPrice
import com.pekomon.cryptoapp.domain.model.PortfolioPosition
import com.pekomon.cryptoapp.domain.portfolio.PortfolioCalculator
import com.pekomon.cryptoapp.ui.MarketLoadState

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
}

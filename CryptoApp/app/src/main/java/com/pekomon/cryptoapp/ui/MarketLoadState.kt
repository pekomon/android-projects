package com.pekomon.cryptoapp.ui

import java.time.LocalDateTime

sealed interface MarketLoadState {
    data object Idle : MarketLoadState
    data object Loading : MarketLoadState

    data class Content(
        val lastUpdated: LocalDateTime,
        val isStale: Boolean = false,
        val message: String? = null
    ) : MarketLoadState

    data class Error(
        val message: String
    ) : MarketLoadState
}

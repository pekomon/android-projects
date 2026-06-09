package com.pekomon.cryptoapp.domain.model

import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption

data class AppSettings(
    val selectedCurrency: Currency,
    val sortOption: SortOption,
    val watchlistIds: Set<String>,
    val favoriteIds: Set<String>
) {
    fun containsVisibleAsset(cryptoId: String): Boolean {
        return cryptoId in watchlistIds || cryptoId in favoriteIds
    }

    companion object {
        val DEFAULT = AppSettings(
            selectedCurrency = Currency.EUR,
            sortOption = SortOption.DEFAULT,
            watchlistIds = emptySet(),
            favoriteIds = emptySet()
        )
    }
}

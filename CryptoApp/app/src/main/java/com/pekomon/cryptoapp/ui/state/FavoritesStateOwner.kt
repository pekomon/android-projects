package com.pekomon.cryptoapp.ui.state

import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.domain.market.CryptoAssetSorter
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.model.MarketPrice
import com.pekomon.cryptoapp.ui.MarketLoadState

class FavoritesStateOwner {
    fun toggleFavorite(
        favoriteIds: Set<String>,
        cryptoId: String
    ): Set<String> {
        return if (cryptoId in favoriteIds) {
            favoriteIds - cryptoId
        } else {
            favoriteIds + cryptoId
        }
    }

    fun isFavorite(
        favoriteIds: Set<String>,
        cryptoId: String
    ): Boolean {
        return cryptoId in favoriteIds
    }

    fun sortedAssets(
        availableAssets: List<CryptoAsset>,
        favoriteIds: Set<String>,
        sortOption: SortOption,
        priceForAsset: (String) -> MarketPrice?
    ): List<CryptoAsset> {
        return CryptoAssetSorter.sort(
            assets = availableAssets.filter { it.id in favoriteIds },
            sortOption = sortOption,
            priceForAsset = priceForAsset
        )
    }

    fun state(
        availableAssets: List<CryptoAsset>,
        favoriteIds: Set<String>,
        prices: Map<String, MarketPrice>,
        selectedCurrency: Currency,
        sortOption: SortOption,
        marketLoadState: MarketLoadState,
        isLoading: Boolean,
        errorMessage: String?
    ): FavoritesUiState {
        return FavoritesUiState(
            assets = sortedAssets(
                availableAssets = availableAssets,
                favoriteIds = favoriteIds,
                sortOption = sortOption,
                priceForAsset = prices::get
            ),
            prices = prices,
            selectedCurrency = selectedCurrency,
            sortOption = sortOption,
            marketLoadState = marketLoadState,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }
}

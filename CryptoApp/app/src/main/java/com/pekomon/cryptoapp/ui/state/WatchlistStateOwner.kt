package com.pekomon.cryptoapp.ui.state

import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.domain.market.CryptoAssetSorter
import com.pekomon.cryptoapp.domain.market.CryptoSelectionSanitizer
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.model.MarketPrice
import com.pekomon.cryptoapp.ui.AssetMetadataSource
import com.pekomon.cryptoapp.ui.MarketLoadState

class WatchlistStateOwner {
    fun sanitizeSelectedAssetIds(
        selectedIds: Set<String>,
        availableAssets: List<CryptoAsset>,
        fallbackIds: Set<String>
    ): Set<String> {
        return CryptoSelectionSanitizer.sanitizeSelection(
            selectedIds = selectedIds,
            availableAssets = availableAssets,
            fallbackIds = fallbackIds
        )
    }

    fun sortedAssets(
        availableAssets: List<CryptoAsset>,
        selectedAssetIds: Set<String>,
        sortOption: SortOption,
        priceForAsset: (String) -> MarketPrice?
    ): List<CryptoAsset> {
        return CryptoAssetSorter.sort(
            assets = availableAssets.filter { it.id in selectedAssetIds },
            sortOption = sortOption,
            priceForAsset = priceForAsset
        )
    }

    fun state(
        availableAssets: List<CryptoAsset>,
        selectedAssetIds: Set<String>,
        favoriteIds: Set<String>,
        prices: Map<String, MarketPrice>,
        selectedCurrency: Currency,
        sortOption: SortOption,
        marketLoadState: MarketLoadState,
        isLoading: Boolean,
        errorMessage: String?,
        assetMetadataSource: AssetMetadataSource?
    ): WatchlistUiState {
        return WatchlistUiState(
            assets = sortedAssets(
                availableAssets = availableAssets,
                selectedAssetIds = selectedAssetIds,
                sortOption = sortOption,
                priceForAsset = prices::get
            ),
            prices = prices,
            favoriteIds = favoriteIds,
            selectedCurrency = selectedCurrency,
            sortOption = sortOption,
            marketLoadState = marketLoadState,
            isLoading = isLoading,
            errorMessage = errorMessage,
            assetMetadataSource = assetMetadataSource
        )
    }
}

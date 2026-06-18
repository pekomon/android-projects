package com.pekomon.cryptoapp.ui.state

import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.ui.AssetMetadataSource

class SettingsStateOwner {
    fun updateSortOption(option: SortOption): SortOption {
        return option
    }

    fun updateCurrency(currency: Currency): Currency {
        return currency
    }

    fun state(
        availableAssets: List<CryptoAsset>,
        selectedAssetIds: Set<String>,
        favoriteIds: Set<String>,
        selectedCurrency: Currency,
        sortOption: SortOption,
        assetMetadataSource: AssetMetadataSource?,
        isLoading: Boolean,
        errorMessage: String?
    ): SettingsUiState {
        return SettingsUiState(
            availableAssets = availableAssets,
            selectedAssetIds = selectedAssetIds,
            favoriteIds = favoriteIds,
            selectedCurrency = selectedCurrency,
            sortOption = sortOption,
            assetMetadataSource = assetMetadataSource,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }
}

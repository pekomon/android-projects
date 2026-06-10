package com.pekomon.cryptoapp.ui.state

import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.model.MarketPrice
import com.pekomon.cryptoapp.domain.model.PortfolioPosition
import com.pekomon.cryptoapp.domain.portfolio.PortfolioHoldingMetrics
import com.pekomon.cryptoapp.domain.portfolio.PortfolioSummaryMetrics
import com.pekomon.cryptoapp.ui.AssetMetadataSource
import com.pekomon.cryptoapp.ui.MarketLoadState

data class WatchlistUiState(
    val assets: List<CryptoAsset>,
    val prices: Map<String, MarketPrice>,
    val favoriteIds: Set<String>,
    val selectedCurrency: Currency,
    val sortOption: SortOption,
    val marketLoadState: MarketLoadState,
    val isLoading: Boolean,
    val errorMessage: String?,
    val assetMetadataSource: AssetMetadataSource?
)

data class FavoritesUiState(
    val assets: List<CryptoAsset>,
    val prices: Map<String, MarketPrice>,
    val selectedCurrency: Currency,
    val sortOption: SortOption,
    val marketLoadState: MarketLoadState,
    val isLoading: Boolean,
    val errorMessage: String?
)

data class PortfolioUiState(
    val positions: List<PortfolioPosition>,
    val summary: PortfolioSummaryMetrics,
    val holdingMetrics: Map<String, PortfolioHoldingMetrics>,
    val selectedCurrency: Currency,
    val marketLoadState: MarketLoadState,
    val isLoading: Boolean,
    val errorMessage: String?
)

data class SettingsUiState(
    val availableAssets: List<CryptoAsset>,
    val selectedAssetIds: Set<String>,
    val favoriteIds: Set<String>,
    val selectedCurrency: Currency,
    val sortOption: SortOption,
    val assetMetadataSource: AssetMetadataSource?,
    val isLoading: Boolean,
    val errorMessage: String?
)

package com.pekomon.cryptoapp.ui

import com.pekomon.cryptoapp.core.logging.CryptoAppLogger
import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.repository.UserPreferencesRepository
import com.pekomon.cryptoapp.ui.state.WatchlistStateOwner
import kotlinx.coroutines.flow.first

class CryptoBootstrapper(
    private val preferencesRepository: UserPreferencesRepository,
    private val assetCatalogLoader: AssetCatalogLoader,
    private val watchlistStateOwner: WatchlistStateOwner,
    private val defaultCryptoIds: Set<String>,
    private val tag: String = "CryptoAppNetwork"
) {
    suspend fun load(): CryptoBootstrapState {
        val selectedCurrency = preferencesRepository.selectedCurrency.first()
        val sortOption = preferencesRepository.sortOption.first()
        val favorites = preferencesRepository.favorites.first()
        val catalog = assetCatalogLoader.load()
        val selectedCryptos = sanitizeSelectedCryptos(
            savedSelection = preferencesRepository.selectedCryptos.first(),
            availableAssets = catalog.assets
        )

        return CryptoBootstrapState(
            selectedCurrency = selectedCurrency,
            sortOption = sortOption,
            favorites = favorites,
            selectedCryptos = selectedCryptos,
            availableCryptos = catalog.assets,
            assetMetadataSource = catalog.source
        )
    }

    suspend fun sanitizeSelection(
        selectedCryptos: Set<String>,
        availableCryptos: List<CryptoAsset>
    ): Set<String> {
        return sanitizeSelectedCryptos(
            savedSelection = selectedCryptos,
            availableAssets = availableCryptos
        )
    }

    private suspend fun sanitizeSelectedCryptos(
        savedSelection: Set<String>,
        availableAssets: List<CryptoAsset>
    ): Set<String> {
        val sanitized = watchlistStateOwner.sanitizeSelectedAssetIds(
            selectedIds = savedSelection,
            availableAssets = availableAssets,
            fallbackIds = defaultCryptoIds
        )

        if (sanitized != savedSelection) {
            CryptoAppLogger.debug(
                tag,
                "sanitizeSelectedCryptos removed=${(savedSelection - sanitized).joinToString(",")} selected=${sanitized.joinToString(",")}"
            )
            preferencesRepository.updateSelectedCryptos(sanitized)
        }

        return sanitized
    }
}

data class CryptoBootstrapState(
    val selectedCurrency: Currency,
    val sortOption: SortOption,
    val favorites: Set<String>,
    val selectedCryptos: Set<String>,
    val availableCryptos: List<CryptoAsset>,
    val assetMetadataSource: AssetMetadataSource
)

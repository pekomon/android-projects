package com.pekomon.cryptoapp.ui

import com.pekomon.cryptoapp.core.logging.CryptoAppLogger
import com.pekomon.cryptoapp.domain.market.DefaultCryptoAssets
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.repository.MarketRepository
import com.pekomon.cryptoapp.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first

class AssetCatalogLoader(
    private val repository: MarketRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val fallbackAssets: List<CryptoAsset> = DefaultCryptoAssets.assets,
    private val tag: String = "CryptoAppNetwork"
) {
    suspend fun load(): AssetCatalogLoadResult {
        return try {
            CryptoAppLogger.debug(tag, "loadAvailableCryptos using live API")
            val liveAssets = repository.getAllAvailableCryptos()
            preferencesRepository.updateCachedCryptoAssets(liveAssets)
            CryptoAppLogger.debug(tag, "loadAvailableCryptos live success count=${liveAssets.size}")
            AssetCatalogLoadResult(
                assets = liveAssets,
                source = AssetMetadataSource.Live
            )
        } catch (error: Exception) {
            CryptoAppLogger.error(tag, "loadAvailableCryptos live failed; trying cache", error)
            val cachedAssets = preferencesRepository.cachedCryptoAssets.first()
            val source = if (cachedAssets.isEmpty()) {
                AssetMetadataSource.Default
            } else {
                AssetMetadataSource.Cache
            }
            val assets = cachedAssets.ifEmpty { fallbackAssets }
            CryptoAppLogger.debug(
                tag,
                "loadAvailableCryptos fallback source=${if (cachedAssets.isEmpty()) "default" else "cache"} count=${assets.size}"
            )
            AssetCatalogLoadResult(
                assets = assets,
                source = source
            )
        }
    }
}

data class AssetCatalogLoadResult(
    val assets: List<CryptoAsset>,
    val source: AssetMetadataSource
)

package com.pekomon.cryptoapp.ui

import com.pekomon.cryptoapp.domain.model.CryptoAsset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AssetCatalogLoaderTest {
    @Test
    fun loadUsesLiveAssetsAndCachesThem() = runTest {
        val liveAssets = listOf(CryptoAsset("bitcoin", "btc", "Bitcoin", 1))
        val preferences = TestPreferencesRepository()
        val repository = TestMarketRepository(assets = liveAssets)

        val result = AssetCatalogLoader(
            repository = repository,
            preferencesRepository = preferences
        ).load()

        assertEquals(AssetMetadataSource.Live, result.source)
        assertEquals(liveAssets, result.assets)
        assertEquals(liveAssets, preferences.cachedCryptoAssets.value)
    }

    @Test
    fun loadFallsBackToCachedAssetsWhenLiveFetchFails() = runTest {
        val cachedAssets = listOf(CryptoAsset("ethereum", "eth", "Ethereum", 2))
        val preferences = TestPreferencesRepository(cachedCryptoAssets = cachedAssets)
        val repository = TestMarketRepository(throwOnAssets = true)

        val result = AssetCatalogLoader(
            repository = repository,
            preferencesRepository = preferences
        ).load()

        assertEquals(AssetMetadataSource.Cache, result.source)
        assertEquals(cachedAssets, result.assets)
    }

    @Test
    fun loadFallsBackToDefaultAssetsWhenCacheIsEmpty() = runTest {
        val fallbackAssets = listOf(CryptoAsset("solana", "sol", "Solana", 3))
        val preferences = TestPreferencesRepository()
        val repository = TestMarketRepository(throwOnAssets = true)

        val result = AssetCatalogLoader(
            repository = repository,
            preferencesRepository = preferences,
            fallbackAssets = fallbackAssets
        ).load()

        assertEquals(AssetMetadataSource.Default, result.source)
        assertEquals(fallbackAssets, result.assets)
    }
}

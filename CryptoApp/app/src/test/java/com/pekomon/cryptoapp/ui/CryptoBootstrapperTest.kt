package com.pekomon.cryptoapp.ui

import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.ui.state.WatchlistStateOwner
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class CryptoBootstrapperTest {
    @Test
    fun loadSanitizesSavedSelectionAgainstAvailableAssets() = runTest {
        val availableAssets = listOf(
            CryptoAsset("bitcoin", "btc", "Bitcoin", 1),
            CryptoAsset("ethereum", "eth", "Ethereum", 2)
        )
        val preferences = TestPreferencesRepository(
            favorites = setOf("ethereum"),
            selectedCryptos = setOf("bitcoin", "missing")
        )
        val bootstrapper = CryptoBootstrapper(
            preferencesRepository = preferences,
            assetCatalogLoader = AssetCatalogLoader(
                repository = TestMarketRepository(assets = availableAssets),
                preferencesRepository = preferences
            ),
            watchlistStateOwner = WatchlistStateOwner(),
            defaultCryptoIds = setOf("bitcoin")
        )

        val result = bootstrapper.load()

        assertEquals(setOf("bitcoin"), result.selectedCryptos)
        assertEquals(setOf("bitcoin"), preferences.selectedCryptos.value)
        assertEquals(setOf("ethereum"), result.favorites)
        assertEquals(AssetMetadataSource.Live, result.assetMetadataSource)
    }

    @Test
    fun sanitizeSelectionFallsBackToDefaultsWhenSelectionIsInvalid() = runTest {
        val availableAssets = listOf(
            CryptoAsset("bitcoin", "btc", "Bitcoin", 1),
            CryptoAsset("ethereum", "eth", "Ethereum", 2)
        )
        val preferences = TestPreferencesRepository(selectedCryptos = setOf("missing"))
        val bootstrapper = CryptoBootstrapper(
            preferencesRepository = preferences,
            assetCatalogLoader = AssetCatalogLoader(
                repository = TestMarketRepository(assets = availableAssets),
                preferencesRepository = preferences
            ),
            watchlistStateOwner = WatchlistStateOwner(),
            defaultCryptoIds = setOf("bitcoin")
        )

        val sanitized = bootstrapper.sanitizeSelection(
            selectedCryptos = setOf("missing"),
            availableCryptos = availableAssets
        )

        assertEquals(setOf("bitcoin"), sanitized)
        assertEquals(setOf("bitcoin"), preferences.selectedCryptos.value)
    }

    @Test
    fun requestBuilderDeduplicatesWatchlistFavoritesAndPortfolioIds() {
        val request = MarketRefreshRequestBuilder().build(
            selectedCryptos = setOf("bitcoin", "ethereum"),
            favorites = setOf("ethereum", "solana"),
            userCryptos = listOf(
                UserCrypto("bitcoin", 1.0, 100.0, TEST_DATE),
                UserCrypto("dogecoin", 2.0, 10.0, TEST_DATE)
            )
        )

        assertEquals(setOf("bitcoin", "ethereum", "solana", "dogecoin"), request.toSet())
        assertEquals(4, request.size)
    }

    private companion object {
        val TEST_DATE: LocalDateTime = LocalDateTime.of(2026, 6, 19, 12, 0)
    }
}

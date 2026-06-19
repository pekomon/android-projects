package com.pekomon.cryptoapp.ui

import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.domain.market.MarketDataResult
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.repository.MarketRepository
import com.pekomon.cryptoapp.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow

internal class TestPreferencesRepository(
    selectedCurrency: Currency = Currency.EUR,
    sortOption: SortOption = SortOption.DEFAULT,
    favorites: Set<String> = emptySet(),
    selectedCryptos: Set<String> = emptySet(),
    userCryptos: List<UserCrypto> = emptyList(),
    cachedCryptoAssets: List<CryptoAsset> = emptyList()
) : UserPreferencesRepository {
    override val selectedCurrency = MutableStateFlow(selectedCurrency)
    override val sortOption = MutableStateFlow(sortOption)
    override val favorites = MutableStateFlow(favorites)
    override val selectedCryptos = MutableStateFlow(selectedCryptos)
    override val userCryptos = MutableStateFlow(userCryptos)
    override val cachedCryptoAssets = MutableStateFlow(cachedCryptoAssets)

    override suspend fun updateSelectedCurrency(currency: Currency) {
        selectedCurrency.value = currency
    }

    override suspend fun updateSortOption(sortOption: SortOption) {
        this.sortOption.value = sortOption
    }

    override suspend fun updateFavorites(favorites: Set<String>) {
        this.favorites.value = favorites
    }

    override suspend fun updateSelectedCryptos(cryptos: Set<String>) {
        selectedCryptos.value = cryptos
    }

    override suspend fun updateUserCryptos(cryptos: List<UserCrypto>) {
        userCryptos.value = cryptos
    }

    override suspend fun updateCachedCryptoAssets(assets: List<CryptoAsset>) {
        cachedCryptoAssets.value = assets
    }
}

internal class TestMarketRepository(
    private val assets: List<CryptoAsset> = emptyList(),
    private val throwOnAssets: Boolean = false
) : MarketRepository {
    override suspend fun getAllAvailableCryptos(): List<CryptoAsset> {
        if (throwOnAssets) {
            error("live asset fetch failed")
        }
        return assets
    }

    override suspend fun getCryptoPrices(
        coinIds: List<String>,
        currency: String
    ): Map<String, Double> = emptyMap()

    override suspend fun getCryptoPricesResult(
        coinIds: List<String>,
        currency: String
    ): MarketDataResult<Map<String, Double>> = error("not used in this test")
}

package com.pekomon.cryptoapp.domain.repository

import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val selectedCurrency: Flow<Currency>
    val sortOption: Flow<SortOption>
    val favorites: Flow<Set<String>>
    val selectedCryptos: Flow<Set<String>>
    val userCryptos: Flow<List<UserCrypto>>
    val cachedCryptoAssets: Flow<List<CryptoAsset>>

    suspend fun updateSelectedCurrency(currency: Currency)
    suspend fun updateSortOption(sortOption: SortOption)
    suspend fun updateFavorites(favorites: Set<String>)
    suspend fun updateSelectedCryptos(cryptos: Set<String>)
    suspend fun updateUserCryptos(cryptos: List<UserCrypto>)
    suspend fun updateCachedCryptoAssets(assets: List<CryptoAsset>)
}

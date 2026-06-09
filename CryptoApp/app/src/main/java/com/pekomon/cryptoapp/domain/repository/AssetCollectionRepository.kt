package com.pekomon.cryptoapp.domain.repository

import com.pekomon.cryptoapp.domain.model.AssetCollectionState
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import kotlinx.coroutines.flow.Flow

interface AssetCollectionRepository {
    val assetCollectionState: Flow<AssetCollectionState>
    val cachedCryptoAssets: Flow<List<CryptoAsset>>

    suspend fun updateWatchlist(ids: Set<String>)
    suspend fun updateFavorites(ids: Set<String>)
    suspend fun updateCachedCryptoAssets(assets: List<CryptoAsset>)
}

package com.pekomon.cryptoapp.domain.model

data class WatchlistAsset(
    val asset: CryptoAsset,
    val price: MarketPrice?,
    val isFavorite: Boolean
)

data class FavoriteAsset(
    val asset: CryptoAsset,
    val price: MarketPrice?
)

data class AssetCollectionState(
    val watchlistIds: Set<String>,
    val favoriteIds: Set<String>
) {
    fun isVisible(cryptoId: String): Boolean {
        return cryptoId in watchlistIds || cryptoId in favoriteIds
    }

    fun isFavorite(cryptoId: String): Boolean {
        return cryptoId in favoriteIds
    }
}

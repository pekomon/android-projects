package com.pekomon.cryptoapp.domain.market

import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.model.MarketPrice

object CryptoAssetSorter {
    fun sort(
        assets: List<CryptoAsset>,
        sortOption: SortOption,
        priceForAsset: (String) -> MarketPrice?
    ): List<CryptoAsset> {
        return when (sortOption) {
            SortOption.NAME_ASC -> assets.sortedBy { it.name }
            SortOption.NAME_DESC -> assets.sortedByDescending { it.name }
            SortOption.SYMBOL_ASC -> assets.sortedBy { it.symbol }
            SortOption.SYMBOL_DESC -> assets.sortedByDescending { it.symbol }
            SortOption.PRICE_ASC -> assets.sortedBy { priceForAsset(it.id)?.currentPrice ?: 0.0 }
            SortOption.PRICE_DESC -> assets.sortedByDescending { priceForAsset(it.id)?.currentPrice ?: 0.0 }
        }
    }
}

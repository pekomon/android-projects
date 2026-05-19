package com.pekomon.cryptoapp.domain.market

import com.pekomon.cryptoapp.domain.model.CryptoAsset

object CryptoSelectionSanitizer {
    fun sanitizeSelection(
        selectedIds: Set<String>,
        availableAssets: List<CryptoAsset>,
        fallbackIds: Set<String>
    ): Set<String> {
        val availableIds = availableAssets.mapTo(mutableSetOf()) { it.id }
        val sanitized = selectedIds.filterTo(mutableSetOf()) { it in availableIds }
        return sanitized.ifEmpty { fallbackIds.filterTo(mutableSetOf()) { it in availableIds } }
    }
}

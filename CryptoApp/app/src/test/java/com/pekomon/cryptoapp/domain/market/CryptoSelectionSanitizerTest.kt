package com.pekomon.cryptoapp.domain.market

import com.pekomon.cryptoapp.domain.model.CryptoAsset
import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoSelectionSanitizerTest {
    @Test
    fun sanitizeSelectionRemovesIdsThatAreNotAvailable() {
        val sanitized = CryptoSelectionSanitizer.sanitizeSelection(
            selectedIds = setOf("bitcoin", "unknown"),
            availableAssets = listOf(asset("bitcoin"), asset("ethereum")),
            fallbackIds = setOf("ethereum")
        )

        assertEquals(setOf("bitcoin"), sanitized)
    }

    @Test
    fun sanitizeSelectionUsesFallbackWhenNothingValidRemains() {
        val sanitized = CryptoSelectionSanitizer.sanitizeSelection(
            selectedIds = setOf("unknown"),
            availableAssets = listOf(asset("bitcoin"), asset("ethereum")),
            fallbackIds = setOf("bitcoin", "missing")
        )

        assertEquals(setOf("bitcoin"), sanitized)
    }

    private fun asset(id: String): CryptoAsset {
        return CryptoAsset(
            id = id,
            symbol = id.take(3),
            name = id,
            marketCapRank = null
        )
    }
}

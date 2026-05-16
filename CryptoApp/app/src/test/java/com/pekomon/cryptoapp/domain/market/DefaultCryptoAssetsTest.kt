package com.pekomon.cryptoapp.domain.market

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultCryptoAssetsTest {
    @Test
    fun assetsContainExpectedWatchlistDefaults() {
        val ids = DefaultCryptoAssets.assets.map { it.id }

        assertEquals(20, ids.size)
        assertTrue("bitcoin" in ids)
        assertTrue("ethereum" in ids)
        assertTrue("dogecoin" in ids)
    }

    @Test
    fun assetsHaveUniqueIds() {
        val ids = DefaultCryptoAssets.assets.map { it.id }

        assertEquals(ids.size, ids.toSet().size)
    }
}

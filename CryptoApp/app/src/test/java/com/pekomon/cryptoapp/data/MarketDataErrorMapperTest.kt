package com.pekomon.cryptoapp.data

import com.pekomon.cryptoapp.domain.market.MarketDataError
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class MarketDataErrorMapperTest {
    @Test
    fun mapsUnauthorizedHttpStatus() {
        val error = MarketDataErrorMapper.fromHttpStatus(401)

        assertTrue(error is MarketDataError.Unauthorized)
    }

    @Test
    fun mapsForbiddenHttpStatus() {
        val error = MarketDataErrorMapper.fromHttpStatus(403)

        assertTrue(error is MarketDataError.Forbidden)
    }

    @Test
    fun mapsRateLimitedHttpStatus() {
        val error = MarketDataErrorMapper.fromHttpStatus(429)

        assertTrue(error is MarketDataError.RateLimited)
    }

    @Test
    fun mapsNetworkException() {
        val error = MarketDataErrorMapper.fromException(IOException("offline"))

        assertTrue(error is MarketDataError.Network)
    }

    @Test
    fun mapsUnexpectedException() {
        val error = MarketDataErrorMapper.fromException(IllegalStateException("bad response"))

        assertTrue(error is MarketDataError.Unknown)
    }
}

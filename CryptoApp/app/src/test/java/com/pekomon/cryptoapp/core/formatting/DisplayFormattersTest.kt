package com.pekomon.cryptoapp.core.formatting

import com.pekomon.cryptoapp.data.Currency
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class DisplayFormattersTest {
    @Test
    fun currencyAmountUsesFixedTwoDecimalPrecision() {
        assertEquals("€1234.50", DisplayFormatters.currencyAmount(1234.5, Currency.EUR))
    }

    @Test
    fun signedCurrencyAmountPrefixesPositiveValues() {
        assertEquals("+$12.30", DisplayFormatters.signedCurrencyAmount(12.3, Currency.USD))
        assertEquals("$-12.30", DisplayFormatters.signedCurrencyAmount(-12.3, Currency.USD))
    }

    @Test
    fun percentageFormatsUnsignedAndSignedValues() {
        assertEquals("4.25%", DisplayFormatters.percentage(4.25))
        assertEquals("+4.25%", DisplayFormatters.signedPercentage(4.25))
        assertEquals("-4.25%", DisplayFormatters.signedPercentage(-4.25))
    }

    @Test
    fun cryptoAmountUsesAdaptivePrecision() {
        assertEquals("0", DisplayFormatters.cryptoAmount(0.0))
        assertEquals("0.00000042", DisplayFormatters.cryptoAmount(0.00000042))
        assertEquals("0.123457", DisplayFormatters.cryptoAmount(0.1234567))
        assertEquals("2.5", DisplayFormatters.cryptoAmount(2.5))
    }

    @Test
    fun dateTimeFormatsAreStable() {
        val dateTime = LocalDateTime.of(2026, 6, 1, 14, 32)

        assertEquals("Last updated 14:32", DisplayFormatters.updateTime(dateTime))
        assertEquals("01.06.2026 14:32", DisplayFormatters.dateTime(dateTime))
    }
}

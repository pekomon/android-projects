package com.pekomon.snapreceipt.core.parsing

import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.ReceiptOcrResult
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HeuristicReceiptParserTest {
    private val parser = HeuristicReceiptParser()

    @Test
    fun parse_extractsMerchantDateTotalAndCurrency() {
        val result = parser.parse(
            ocrResult = ReceiptOcrResult(
                fullText = """
                    CAFE CENTRAL
                    28/06/2026
                    LATTE 4.20
                    CAKE 8.60
                    TOTAL EUR 12.80
                """.trimIndent(),
                lineBlocks = listOf(
                    "CAFE CENTRAL",
                    "28/06/2026",
                    "LATTE 4.20",
                    "CAKE 8.60",
                    "TOTAL EUR 12.80"
                )
            )
        )

        assertEquals("CAFE CENTRAL", result.merchantName)
        assertEquals(LocalDate.of(2026, 6, 28), result.transactionDate)
        assertEquals(BigDecimal("12.80"), result.totalAmount)
        assertEquals(ReceiptCurrency.EUR, result.currency)
    }

    @Test
    fun parse_usesFallbackCurrencyWhenTextHasNoCurrencyCode() {
        val result = parser.parse(
            ocrResult = ReceiptOcrResult(
                fullText = """
                    BAKERY NORTH
                    27.06.2026
                    TOTAL 9.40
                """.trimIndent(),
                lineBlocks = listOf(
                    "BAKERY NORTH",
                    "27.06.2026",
                    "TOTAL 9.40"
                )
            ),
            fallbackCurrency = ReceiptCurrency.SEK
        )

        assertEquals(ReceiptCurrency.SEK, result.currency)
    }

    @Test
    fun parse_prefersLargestTotalOnTotalLine() {
        val result = parser.parse(
            ocrResult = ReceiptOcrResult(
                fullText = """
                    MARKET HALL
                    30/06/2026
                    SUBTOTAL 10.00
                    VAT 2.40
                    TOTAL 12.40
                """.trimIndent(),
                lineBlocks = listOf(
                    "MARKET HALL",
                    "30/06/2026",
                    "SUBTOTAL 10.00",
                    "VAT 2.40",
                    "TOTAL 12.40"
                )
            )
        )

        assertEquals(BigDecimal("12.40"), result.totalAmount)
    }

    @Test
    fun parse_returnsNullsWhenStructuredSignalsAreMissing() {
        val result = parser.parse(
            ocrResult = ReceiptOcrResult(
                fullText = "12345\n67890",
                lineBlocks = listOf("12345", "67890")
            )
        )

        assertNull(result.merchantName)
        assertNull(result.transactionDate)
        assertNull(result.totalAmount)
        assertNull(result.currency)
    }
}

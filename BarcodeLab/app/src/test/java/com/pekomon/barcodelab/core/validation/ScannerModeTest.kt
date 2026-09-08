package com.pekomon.barcodelab.core.validation

import com.pekomon.barcodelab.domain.model.BarcodeFormat
import com.pekomon.barcodelab.domain.model.ScannerMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerModeTest {
    @Test
    fun twoDimensionalModeOnlyContains2DFormats() {
        assertEquals(
            setOf(
                BarcodeFormat.QrCode,
                BarcodeFormat.Pdf417,
                BarcodeFormat.Aztec,
                BarcodeFormat.DataMatrix,
            ),
            ScannerMode.TwoDimensional.formats,
        )
    }

    @Test
    fun productModeContainsRetailFormats() {
        assertEquals(
            setOf(
                BarcodeFormat.Ean13,
                BarcodeFormat.Ean8,
                BarcodeFormat.UpcA,
                BarcodeFormat.UpcE,
            ),
            ScannerMode.Product.formats,
        )
    }

    @Test
    fun logisticsModeContainsLinearOperationalFormats() {
        assertEquals(
            setOf(
                BarcodeFormat.Code128,
                BarcodeFormat.Code39,
                BarcodeFormat.Code93,
                BarcodeFormat.Codabar,
                BarcodeFormat.Itf,
            ),
            ScannerMode.Logistics.formats,
        )
    }

    @Test
    fun allFormatsModeContainsEveryExplicitModeFormat() {
        val expected = ScannerMode.TwoDimensional.formats +
            ScannerMode.Product.formats +
            ScannerMode.Logistics.formats

        assertEquals(expected, ScannerMode.AllFormats.formats)
        assertTrue(ScannerMode.AllFormats.formats.contains(BarcodeFormat.QrCode))
        assertTrue(ScannerMode.AllFormats.formats.contains(BarcodeFormat.Ean13))
        assertTrue(ScannerMode.AllFormats.formats.contains(BarcodeFormat.Code128))
        assertFalse(ScannerMode.AllFormats.formats.contains(BarcodeFormat.Unknown))
    }
}

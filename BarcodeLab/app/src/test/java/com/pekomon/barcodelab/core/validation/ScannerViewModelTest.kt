package com.pekomon.barcodelab.core.validation

import com.pekomon.barcodelab.domain.model.BarcodeFormat
import com.pekomon.barcodelab.domain.model.DetectedBarcode
import com.pekomon.barcodelab.domain.model.PayloadKind
import com.pekomon.barcodelab.feature.scanner.ScannerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerViewModelTest {
    @Test
    fun pausesAfterDetectionAndStoresResult() {
        val viewModel = ScannerViewModel(clockMillis = { 1234L })

        viewModel.onBarcodeDetected(
            DetectedBarcode(
                format = BarcodeFormat.QrCode,
                rawValue = "https://example.com",
            ),
        )

        val state = viewModel.uiState.value
        assertTrue(state.isPaused)
        assertEquals(PayloadKind.Url, state.lastResult?.payload?.kind)
        assertEquals(1, state.recentScans.size)
    }

    @Test
    fun ignoresNewDetectionWhilePaused() {
        val viewModel = ScannerViewModel(clockMillis = { 1L })
        viewModel.onBarcodeDetected(DetectedBarcode(BarcodeFormat.QrCode, "https://first.example"))

        viewModel.onBarcodeDetected(DetectedBarcode(BarcodeFormat.QrCode, "https://second.example"))

        assertEquals("https://first.example", viewModel.uiState.value.lastResult?.payload?.rawValue)
    }

    @Test
    fun resumeAllowsNextDetection() {
        val viewModel = ScannerViewModel(clockMillis = { 1L })
        viewModel.onBarcodeDetected(DetectedBarcode(BarcodeFormat.QrCode, "https://first.example"))

        viewModel.resumeScanning()
        viewModel.onBarcodeDetected(DetectedBarcode(BarcodeFormat.QrCode, "https://second.example"))

        assertEquals("https://second.example", viewModel.uiState.value.lastResult?.payload?.rawValue)
        assertEquals(2, viewModel.uiState.value.recentScans.size)
    }
}

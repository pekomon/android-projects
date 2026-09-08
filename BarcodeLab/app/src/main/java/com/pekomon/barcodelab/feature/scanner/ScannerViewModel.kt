package com.pekomon.barcodelab.feature.scanner

import androidx.lifecycle.ViewModel
import com.pekomon.barcodelab.core.validation.PayloadClassifier
import com.pekomon.barcodelab.core.validation.PayloadValidator
import com.pekomon.barcodelab.domain.model.DetectedBarcode
import com.pekomon.barcodelab.domain.model.ScanResult
import com.pekomon.barcodelab.domain.model.ScannerMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ScannerViewModel(
    private val classifier: PayloadClassifier = PayloadClassifier(),
    private val validator: PayloadValidator = PayloadValidator(),
    private val clockMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState

    fun onBarcodeDetected(barcode: DetectedBarcode) {
        _uiState.update { current ->
            if (current.isPaused || barcode.rawValue.isBlank()) {
                current
            } else {
                val payload = classifier.classify(
                    rawValue = barcode.rawValue,
                    displayValue = barcode.displayValue,
                    sourceFormat = barcode.format,
                )
                val result = ScanResult(
                    detectedBarcode = barcode,
                    payload = payload,
                    validation = validator.validate(payload),
                    detectedAtMillis = clockMillis(),
                )
                current.copy(
                    isPaused = true,
                    lastResult = result,
                    analyzerError = null,
                    recentScans = (listOf(result) + current.recentScans)
                        .distinctBy { it.detectedBarcode.rawValue }
                        .take(MaxRecentScans),
                )
            }
        }
    }

    fun resumeScanning() {
        _uiState.update { it.copy(isPaused = false, analyzerError = null) }
    }

    fun selectMode(mode: ScannerMode) {
        _uiState.update {
            if (it.scannerMode == mode) {
                it
            } else {
                it.copy(
                    scannerMode = mode,
                    isPaused = false,
                    lastResult = null,
                    analyzerError = null,
                )
            }
        }
    }

    fun onAnalyzerError(throwable: Throwable) {
        _uiState.update {
            it.copy(analyzerError = throwable.message ?: "Scanner analysis failed")
        }
    }

    companion object {
        private const val MaxRecentScans = 5
    }
}

data class ScannerUiState(
    val scannerMode: ScannerMode = ScannerMode.TwoDimensional,
    val isPaused: Boolean = false,
    val lastResult: ScanResult? = null,
    val recentScans: List<ScanResult> = emptyList(),
    val analyzerError: String? = null,
)

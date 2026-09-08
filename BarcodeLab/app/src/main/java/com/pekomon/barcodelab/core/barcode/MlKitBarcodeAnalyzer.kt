package com.pekomon.barcodelab.core.barcode

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UNKNOWN
import com.google.mlkit.vision.common.InputImage
import com.pekomon.barcodelab.domain.model.BarcodeBounds
import com.pekomon.barcodelab.domain.model.BarcodeFormat
import com.pekomon.barcodelab.domain.model.DetectedBarcode
import com.pekomon.barcodelab.domain.model.ScannerMode
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class MlKitBarcodeAnalyzer(
    private val isScanningEnabled: () -> Boolean,
    private val scannerMode: () -> ScannerMode,
    private val onBarcodeDetected: (DetectedBarcode) -> Unit,
    private val onAnalyzerError: (Throwable) -> Unit,
) : ImageAnalysis.Analyzer, Closeable {
    private val processing = AtomicBoolean(false)
    private var activeMode = scannerMode()
    private var scanner = createScanner(activeMode)

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (!isScanningEnabled() || mediaImage == null) {
            imageProxy.close()
            return
        }
        if (!processing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scannerForCurrentMode().process(image)
            .addOnSuccessListener { barcodes ->
                barcodes
                    .firstOrNull { !it.rawValue.isNullOrBlank() }
                    ?.toDetectedBarcode()
                    ?.let(onBarcodeDetected)
            }
            .addOnFailureListener(onAnalyzerError)
            .addOnCompleteListener {
                processing.set(false)
                imageProxy.close()
            }
    }

    override fun close() {
        scanner.close()
    }

    private fun scannerForCurrentMode(): BarcodeScanner {
        val requestedMode = scannerMode()
        if (requestedMode != activeMode) {
            scanner.close()
            activeMode = requestedMode
            scanner = createScanner(requestedMode)
        }
        return scanner
    }
}

private fun createScanner(mode: ScannerMode): BarcodeScanner {
    val formats = mode.formats.map { it.toMlKitFormat() }
    return BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(formats.first(), *formats.drop(1).toIntArray())
            .build(),
    )
}

private fun Barcode.toDetectedBarcode(): DetectedBarcode =
    DetectedBarcode(
        format = format.toDomainFormat(),
        rawValue = rawValue.orEmpty(),
        displayValue = displayValue.orEmpty().ifBlank { rawValue.orEmpty() },
        bounds = boundingBox?.let { rect ->
            BarcodeBounds(
                left = rect.left,
                top = rect.top,
                right = rect.right,
                bottom = rect.bottom,
            )
        },
    )

private fun Int.toDomainFormat(): BarcodeFormat =
    when (this) {
        FORMAT_CODE_128 -> BarcodeFormat.Code128
        FORMAT_CODE_39 -> BarcodeFormat.Code39
        FORMAT_CODE_93 -> BarcodeFormat.Code93
        FORMAT_CODABAR -> BarcodeFormat.Codabar
        FORMAT_QR_CODE -> BarcodeFormat.QrCode
        FORMAT_EAN_13 -> BarcodeFormat.Ean13
        FORMAT_EAN_8 -> BarcodeFormat.Ean8
        FORMAT_ITF -> BarcodeFormat.Itf
        FORMAT_UPC_A -> BarcodeFormat.UpcA
        FORMAT_UPC_E -> BarcodeFormat.UpcE
        FORMAT_PDF417 -> BarcodeFormat.Pdf417
        FORMAT_AZTEC -> BarcodeFormat.Aztec
        FORMAT_DATA_MATRIX -> BarcodeFormat.DataMatrix
        FORMAT_UNKNOWN -> BarcodeFormat.Unknown
        else -> BarcodeFormat.Unknown
    }

private fun BarcodeFormat.toMlKitFormat(): Int =
    when (this) {
        BarcodeFormat.Code128 -> FORMAT_CODE_128
        BarcodeFormat.Code39 -> FORMAT_CODE_39
        BarcodeFormat.Code93 -> FORMAT_CODE_93
        BarcodeFormat.Codabar -> FORMAT_CODABAR
        BarcodeFormat.QrCode -> FORMAT_QR_CODE
        BarcodeFormat.Ean13 -> FORMAT_EAN_13
        BarcodeFormat.Ean8 -> FORMAT_EAN_8
        BarcodeFormat.Itf -> FORMAT_ITF
        BarcodeFormat.UpcA -> FORMAT_UPC_A
        BarcodeFormat.UpcE -> FORMAT_UPC_E
        BarcodeFormat.Pdf417 -> FORMAT_PDF417
        BarcodeFormat.Aztec -> FORMAT_AZTEC
        BarcodeFormat.DataMatrix -> FORMAT_DATA_MATRIX
        BarcodeFormat.Unknown -> FORMAT_UNKNOWN
    }

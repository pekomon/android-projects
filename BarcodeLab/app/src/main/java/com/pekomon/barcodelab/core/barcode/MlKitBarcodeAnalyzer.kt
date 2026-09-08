package com.pekomon.barcodelab.core.barcode

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UNKNOWN
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage
import com.pekomon.barcodelab.domain.model.BarcodeBounds
import com.pekomon.barcodelab.domain.model.BarcodeFormat
import com.pekomon.barcodelab.domain.model.DetectedBarcode
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class MlKitBarcodeAnalyzer(
    private val isScanningEnabled: () -> Boolean,
    private val onBarcodeDetected: (DetectedBarcode) -> Unit,
    private val onAnalyzerError: (Throwable) -> Unit,
) : ImageAnalysis.Analyzer, Closeable {
    private val processing = AtomicBoolean(false)
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                FORMAT_QR_CODE,
                FORMAT_PDF417,
                FORMAT_AZTEC,
                FORMAT_DATA_MATRIX,
            )
            .build(),
    )

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
        scanner.process(image)
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
        FORMAT_QR_CODE -> BarcodeFormat.QrCode
        FORMAT_PDF417 -> BarcodeFormat.Pdf417
        FORMAT_AZTEC -> BarcodeFormat.Aztec
        FORMAT_DATA_MATRIX -> BarcodeFormat.DataMatrix
        FORMAT_UNKNOWN -> BarcodeFormat.Unknown
        else -> BarcodeFormat.Unknown
    }

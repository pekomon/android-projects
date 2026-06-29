package com.pekomon.snapreceipt.core.ocr

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.ReceiptOcrResult
import com.pekomon.snapreceipt.domain.ocr.ReceiptOcrEngine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class MlKitReceiptOcrEngine(
    private val context: Context
) : ReceiptOcrEngine {
    override suspend fun extractText(image: ReceiptImage): ReceiptOcrResult {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val inputImage = InputImage.fromFilePath(context, Uri.parse(image.localPath))
            val result = recognizer.process(inputImage).awaitResult()
            ReceiptOcrResult(
                fullText = result.text,
                lineBlocks = result.textBlocks.flatMap { block -> block.lines.map { it.text } },
                confidenceHint = null
            )
        } finally {
            recognizer.close()
        }
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { throwable ->
        continuation.resumeWithException(throwable)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}

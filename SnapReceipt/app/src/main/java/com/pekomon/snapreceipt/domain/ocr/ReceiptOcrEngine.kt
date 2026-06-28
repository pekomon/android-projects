package com.pekomon.snapreceipt.domain.ocr

import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.ReceiptOcrResult

interface ReceiptOcrEngine {
    suspend fun extractText(image: ReceiptImage): ReceiptOcrResult
}

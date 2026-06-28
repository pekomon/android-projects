package com.pekomon.snapreceipt.domain.model

data class ReceiptOcrResult(
    val fullText: String,
    val lineBlocks: List<String>,
    val confidenceHint: Float? = null
) {
    val cleanedText: String
        get() = fullText.trim()
}

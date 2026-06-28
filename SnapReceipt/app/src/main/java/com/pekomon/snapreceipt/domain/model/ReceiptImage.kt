package com.pekomon.snapreceipt.domain.model

data class ReceiptImage(
    val localPath: String,
    val source: ReceiptSource,
    val mimeType: String? = null,
    val widthPx: Int? = null,
    val heightPx: Int? = null
)

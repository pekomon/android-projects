package com.pekomon.snapreceipt.domain.model

data class SaveReceiptRequest(
    val draft: ReceiptDraft,
    val imageCompressionQuality: Int
)

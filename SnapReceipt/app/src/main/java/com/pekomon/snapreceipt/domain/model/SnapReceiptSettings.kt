package com.pekomon.snapreceipt.domain.model

data class SnapReceiptSettings(
    val defaultCurrency: ReceiptCurrency = ReceiptCurrency.EUR,
    val imageCompressionQuality: Int = 85
)

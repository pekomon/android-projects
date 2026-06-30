package com.pekomon.snapreceipt.feature.review

data class ReviewDraftFormState(
    val merchantName: String = "",
    val transactionDate: String = "",
    val totalAmount: String = "",
    val currencyCode: String = "",
    val notes: String = "",
    val rawOcrPreview: String = ""
)

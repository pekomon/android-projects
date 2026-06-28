package com.pekomon.snapreceipt.domain.model

data class ReceiptDraft(
    val image: ReceiptImage,
    val ocrResult: ReceiptOcrResult? = null,
    val parsedFields: ParsedReceiptFields = ParsedReceiptFields(),
    val notes: String = ""
) {
    val isReadyToSave: Boolean
        get() = !parsedFields.merchantName.isNullOrBlank() &&
            parsedFields.transactionDate != null &&
            parsedFields.totalAmount != null &&
            parsedFields.currency != null
}

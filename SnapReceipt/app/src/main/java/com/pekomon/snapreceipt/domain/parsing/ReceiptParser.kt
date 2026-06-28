package com.pekomon.snapreceipt.domain.parsing

import com.pekomon.snapreceipt.domain.model.ParsedReceiptFields
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.ReceiptOcrResult

interface ReceiptParser {
    fun parse(
        ocrResult: ReceiptOcrResult,
        fallbackCurrency: ReceiptCurrency? = null
    ): ParsedReceiptFields
}

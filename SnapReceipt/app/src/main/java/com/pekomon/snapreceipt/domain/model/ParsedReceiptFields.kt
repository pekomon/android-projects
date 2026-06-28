package com.pekomon.snapreceipt.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class ParsedReceiptFields(
    val merchantName: String? = null,
    val transactionDate: LocalDate? = null,
    val totalAmount: BigDecimal? = null,
    val currency: ReceiptCurrency? = null
)

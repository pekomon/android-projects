package com.pekomon.snapreceipt.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class Receipt(
    val id: String,
    val merchantName: String,
    val transactionDate: LocalDate,
    val totalAmount: BigDecimal,
    val currency: ReceiptCurrency,
    val image: ReceiptImage,
    val rawOcrText: String,
    val notes: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

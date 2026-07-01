package com.pekomon.snapreceipt.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey val id: String,
    val merchantName: String,
    val transactionDateIso: String,
    val totalAmount: String,
    val currencyCode: String,
    val imageLocalPath: String,
    val imageSource: String,
    val imageMimeType: String?,
    val imageWidthPx: Int?,
    val imageHeightPx: Int?,
    val rawOcrText: String,
    val notes: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

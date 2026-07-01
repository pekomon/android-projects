package com.pekomon.snapreceipt.data.local

import com.pekomon.snapreceipt.domain.model.Receipt
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.ReceiptSource
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

fun ReceiptEntity.toDomain(): Receipt {
    return Receipt(
        id = id,
        merchantName = merchantName,
        transactionDate = LocalDate.parse(transactionDateIso),
        totalAmount = BigDecimal(totalAmount),
        currency = requireNotNull(ReceiptCurrency.fromCode(currencyCode)),
        image = ReceiptImage(
            localPath = imageLocalPath,
            source = ReceiptSource.valueOf(imageSource),
            mimeType = imageMimeType,
            widthPx = imageWidthPx,
            heightPx = imageHeightPx
        ),
        rawOcrText = rawOcrText,
        notes = notes,
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis)
    )
}

fun Receipt.toEntity(): ReceiptEntity {
    return ReceiptEntity(
        id = id,
        merchantName = merchantName,
        transactionDateIso = transactionDate.toString(),
        totalAmount = totalAmount.toPlainString(),
        currencyCode = currency.code,
        imageLocalPath = image.localPath,
        imageSource = image.source.name,
        imageMimeType = image.mimeType,
        imageWidthPx = image.widthPx,
        imageHeightPx = image.heightPx,
        rawOcrText = rawOcrText,
        notes = notes,
        createdAtEpochMillis = createdAt.toEpochMilli(),
        updatedAtEpochMillis = updatedAt.toEpochMilli()
    )
}

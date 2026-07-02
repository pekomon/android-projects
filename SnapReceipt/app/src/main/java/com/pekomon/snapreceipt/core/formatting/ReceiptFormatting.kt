package com.pekomon.snapreceipt.core.formatting

import com.pekomon.snapreceipt.domain.model.Receipt
import java.math.RoundingMode
import java.time.format.DateTimeFormatter

private val receiptDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

fun Receipt.formattedDate(): String = transactionDate.format(receiptDateFormatter)

fun Receipt.formattedTotal(): String {
    val normalizedAmount = totalAmount.setScale(2, RoundingMode.HALF_UP).toPlainString()
    return "${currency.code} $normalizedAmount"
}

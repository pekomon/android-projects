package com.pekomon.snapreceipt.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptDraftTest {
    private val sampleImage = ReceiptImage(
        localPath = "/tmp/receipt.jpg",
        source = ReceiptSource.PHOTO_PICKER
    )

    @Test
    fun isReadyToSave_requiresStructuredFields() {
        val draft = ReceiptDraft(image = sampleImage)

        assertFalse(draft.isReadyToSave)
    }

    @Test
    fun isReadyToSave_isTrueWhenRequiredFieldsExist() {
        val draft = ReceiptDraft(
            image = sampleImage,
            parsedFields = ParsedReceiptFields(
                merchantName = "Cafe Central",
                transactionDate = LocalDate.of(2026, 6, 28),
                totalAmount = BigDecimal("12.80"),
                currency = ReceiptCurrency.EUR
            )
        )

        assertTrue(draft.isReadyToSave)
    }
}

package com.pekomon.snapreceipt.domain.storage

import com.pekomon.snapreceipt.domain.model.ReceiptImage

interface ReceiptImageStorage {
    suspend fun persistImportedImage(image: ReceiptImage): ReceiptImage

    suspend fun deleteStoredImage(image: ReceiptImage)
}

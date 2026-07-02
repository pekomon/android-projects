package com.pekomon.snapreceipt.domain.storage

import com.pekomon.snapreceipt.domain.model.ReceiptImage

interface ReceiptImageStorage {
    suspend fun persistImportedImage(
        image: ReceiptImage,
        compressionQuality: Int
    ): ReceiptImage

    suspend fun deleteStoredImage(image: ReceiptImage)
}

package com.pekomon.snapreceipt.domain.repository

import com.pekomon.snapreceipt.domain.model.Receipt
import com.pekomon.snapreceipt.domain.model.SaveReceiptRequest
import kotlinx.coroutines.flow.Flow

interface ReceiptRepository {
    fun observeReceipts(): Flow<List<Receipt>>

    suspend fun getReceipt(receiptId: String): Receipt?

    suspend fun saveReceipt(request: SaveReceiptRequest): Receipt

    suspend fun deleteReceipt(receiptId: String)
}

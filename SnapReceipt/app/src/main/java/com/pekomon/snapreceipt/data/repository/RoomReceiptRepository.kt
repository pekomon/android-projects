package com.pekomon.snapreceipt.data.repository

import com.pekomon.snapreceipt.data.local.ReceiptDao
import com.pekomon.snapreceipt.data.local.toDomain
import com.pekomon.snapreceipt.data.local.toEntity
import com.pekomon.snapreceipt.domain.model.Receipt
import com.pekomon.snapreceipt.domain.model.SaveReceiptRequest
import com.pekomon.snapreceipt.domain.repository.ReceiptRepository
import com.pekomon.snapreceipt.domain.storage.ReceiptImageStorage
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomReceiptRepository(
    private val receiptDao: ReceiptDao,
    private val imageStorage: ReceiptImageStorage
) : ReceiptRepository {
    override fun observeReceipts(): Flow<List<Receipt>> {
        return receiptDao.observeReceipts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReceipt(receiptId: String): Receipt? {
        return receiptDao.getReceipt(receiptId)?.toDomain()
    }

    override suspend fun saveReceipt(request: SaveReceiptRequest): Receipt {
        val persistedImage = imageStorage.persistImportedImage(request.draft.image)
        val now = Instant.now()
        val receipt = Receipt(
            id = UUID.randomUUID().toString(),
            merchantName = requireNotNull(request.draft.parsedFields.merchantName),
            transactionDate = requireNotNull(request.draft.parsedFields.transactionDate),
            totalAmount = requireNotNull(request.draft.parsedFields.totalAmount),
            currency = requireNotNull(request.draft.parsedFields.currency),
            image = persistedImage,
            rawOcrText = request.draft.ocrResult?.cleanedText.orEmpty(),
            notes = request.draft.notes,
            createdAt = now,
            updatedAt = now
        )
        receiptDao.upsertReceipt(receipt.toEntity())
        return receipt
    }

    override suspend fun deleteReceipt(receiptId: String) {
        val existingReceipt = receiptDao.getReceipt(receiptId)?.toDomain() ?: return
        receiptDao.deleteReceiptById(receiptId)
        imageStorage.deleteStoredImage(existingReceipt.image)
    }
}

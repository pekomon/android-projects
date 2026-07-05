package com.pekomon.snapreceipt.data.repository

import com.pekomon.snapreceipt.data.local.ReceiptDao
import com.pekomon.snapreceipt.data.local.ReceiptEntity
import com.pekomon.snapreceipt.data.local.toEntity
import com.pekomon.snapreceipt.domain.model.ParsedReceiptFields
import com.pekomon.snapreceipt.domain.model.Receipt
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.ReceiptDraft
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.ReceiptOcrResult
import com.pekomon.snapreceipt.domain.model.ReceiptSource
import com.pekomon.snapreceipt.domain.model.SaveReceiptRequest
import com.pekomon.snapreceipt.domain.storage.ReceiptImageStorage
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RoomReceiptRepositoryTest {
    @Test
    fun saveReceipt_persistsMappedReceiptAndUsesConfiguredCompressionQuality() = runTest {
        val dao = FakeReceiptDao()
        val imageStorage = FakeReceiptImageStorage()
        val repository = RoomReceiptRepository(
            receiptDao = dao,
            imageStorage = imageStorage
        )

        val saved = repository.saveReceipt(
            SaveReceiptRequest(
                draft = readyDraft(),
                imageCompressionQuality = 72
            )
        )

        assertEquals(72, imageStorage.lastCompressionQuality)
        assertNotNull(imageStorage.lastPersistedImage)
        assertEquals(saved.id, dao.getReceipt(saved.id)?.id)
        assertEquals("file:///persisted-receipt.jpg", saved.image.localPath)
        assertEquals(
            listOf(saved.id),
            repository.observeReceipts().first().map { it.id }
        )
    }

    @Test
    fun deleteReceipt_removesReceiptAndDeletesStoredImage() = runTest {
        val dao = FakeReceiptDao()
        val imageStorage = FakeReceiptImageStorage()
        val repository = RoomReceiptRepository(
            receiptDao = dao,
            imageStorage = imageStorage
        )
        val existing = sampleReceipt(id = "existing-1")
        dao.upsertReceipt(existing.toEntity())

        repository.deleteReceipt(existing.id)

        assertNull(dao.getReceipt(existing.id))
        assertEquals(existing.image, imageStorage.deletedImage)
    }

    private fun readyDraft() = ReceiptDraft(
        image = ReceiptImage(
            localPath = "file:///captured.jpg",
            source = ReceiptSource.CAMERA,
            mimeType = "image/jpeg"
        ),
        ocrResult = ReceiptOcrResult(
            fullText = "Northwind Cafe\n18.40 EUR",
            lineBlocks = listOf("Northwind Cafe", "18.40 EUR")
        ),
        parsedFields = ParsedReceiptFields(
            merchantName = "Northwind Cafe",
            transactionDate = LocalDate.of(2026, 7, 5),
            totalAmount = BigDecimal("18.40"),
            currency = ReceiptCurrency.EUR
        ),
        notes = "Lunch meeting"
    )

    private fun sampleReceipt(id: String) = Receipt(
        id = id,
        merchantName = "Northwind Cafe",
        transactionDate = LocalDate.of(2026, 7, 5),
        totalAmount = BigDecimal("18.40"),
        currency = ReceiptCurrency.EUR,
        image = ReceiptImage(
            localPath = "file:///persisted-receipt.jpg",
            source = ReceiptSource.CAMERA,
            mimeType = "image/jpeg"
        ),
        rawOcrText = "Northwind Cafe\n18.40 EUR",
        notes = "Lunch meeting",
        createdAt = Instant.ofEpochMilli(1_000),
        updatedAt = Instant.ofEpochMilli(1_000)
    )
}

private class FakeReceiptDao : ReceiptDao {
    private val receipts = linkedMapOf<String, ReceiptEntity>()
    private val receiptsFlow = MutableStateFlow(emptyList<ReceiptEntity>())

    override fun observeReceipts(): Flow<List<ReceiptEntity>> = receiptsFlow

    override suspend fun getReceipt(receiptId: String): ReceiptEntity? = receipts[receiptId]

    override suspend fun upsertReceipt(receipt: ReceiptEntity) {
        receipts[receipt.id] = receipt
        publish()
    }

    override suspend fun deleteReceiptById(receiptId: String) {
        receipts.remove(receiptId)
        publish()
    }

    private fun publish() {
        receiptsFlow.value = receipts.values.toList()
    }
}

private class FakeReceiptImageStorage : ReceiptImageStorage {
    var lastPersistedImage: ReceiptImage? = null
    var lastCompressionQuality: Int? = null
    var deletedImage: ReceiptImage? = null

    override suspend fun persistImportedImage(
        image: ReceiptImage,
        compressionQuality: Int
    ): ReceiptImage {
        lastPersistedImage = image
        lastCompressionQuality = compressionQuality
        return image.copy(localPath = "file:///persisted-receipt.jpg")
    }

    override suspend fun deleteStoredImage(image: ReceiptImage) {
        deletedImage = image
    }
}

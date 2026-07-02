package com.pekomon.snapreceipt.feature.capture

import com.pekomon.snapreceipt.domain.model.ParsedReceiptFields
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.Receipt
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.ReceiptOcrResult
import com.pekomon.snapreceipt.domain.model.ReceiptSource
import com.pekomon.snapreceipt.domain.model.SaveReceiptRequest
import com.pekomon.snapreceipt.domain.model.SnapReceiptSettings
import com.pekomon.snapreceipt.domain.ocr.ReceiptOcrEngine
import com.pekomon.snapreceipt.domain.parsing.ReceiptParser
import com.pekomon.snapreceipt.domain.repository.ReceiptRepository
import com.pekomon.snapreceipt.domain.repository.SnapReceiptSettingsRepository
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun importedImage_createsDraftAfterSuccessfulOcr() = runTest(dispatcher) {
        val viewModel = CaptureViewModel(
            ocrEngine = object : ReceiptOcrEngine {
                override suspend fun extractText(image: ReceiptImage): ReceiptOcrResult {
                    return ReceiptOcrResult(
                        fullText = "Cafe Central\n12.80 EUR",
                        lineBlocks = listOf("Cafe Central", "12.80 EUR")
                    )
                }
            },
            receiptParser = FakeReceiptParser(),
            receiptRepository = FakeReceiptRepository(),
            settingsRepository = FakeSettingsRepository()
        )

        viewModel.onImageImported(
            uriString = "content://receipt/1",
            source = ReceiptSource.PHOTO_PICKER,
            mimeType = "image/jpeg"
        )

        dispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.selectedImage)
        assertNotNull(uiState.draft)
        assertNotNull(uiState.reviewForm)
        assertNull(uiState.ocrErrorMessage)
        assertEquals(2, uiState.draft?.ocrResult?.lineBlocks?.size)
        assertEquals("Cafe Central", uiState.draft?.parsedFields?.merchantName)
    }

    @Test
    fun importedImage_surfacesOcrFailure() = runTest(dispatcher) {
        val viewModel = CaptureViewModel(
            ocrEngine = object : ReceiptOcrEngine {
                override suspend fun extractText(image: ReceiptImage): ReceiptOcrResult {
                    error("OCR unavailable")
                }
            },
            receiptParser = FakeReceiptParser(),
            receiptRepository = FakeReceiptRepository(),
            settingsRepository = FakeSettingsRepository()
        )

        viewModel.onImageImported(
            uriString = "content://receipt/2",
            source = ReceiptSource.FILE_IMPORT,
            mimeType = "image/png"
        )

        dispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.selectedImage)
        assertNull(uiState.draft)
        assertEquals("OCR unavailable", uiState.ocrErrorMessage)
        assertTrue(!uiState.isRunningOcr)
    }

    @Test
    fun saveReviewedReceipt_persistsReadyDraft() = runTest(dispatcher) {
        val repository = FakeReceiptRepository()
        val viewModel = CaptureViewModel(
            ocrEngine = object : ReceiptOcrEngine {
                override suspend fun extractText(image: ReceiptImage): ReceiptOcrResult {
                    return ReceiptOcrResult(
                        fullText = "Cafe Central\n12.80 EUR",
                        lineBlocks = listOf("Cafe Central", "12.80 EUR")
                    )
                }
            },
            receiptParser = FakeReceiptParser(),
            receiptRepository = repository,
            settingsRepository = FakeSettingsRepository()
        )

        viewModel.onImageImported(
            uriString = "content://receipt/3",
            source = ReceiptSource.PHOTO_PICKER,
            mimeType = "image/jpeg"
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.saveReviewedReceipt()
        dispatcher.scheduler.advanceUntilIdle()

        assertNotNull(repository.savedRequest)
        assertNotNull(viewModel.uiState.value.lastSavedReceipt)
        assertNull(viewModel.uiState.value.draft)
    }

    private class FakeReceiptParser : ReceiptParser {
        override fun parse(
            ocrResult: ReceiptOcrResult,
            fallbackCurrency: ReceiptCurrency?
        ): ParsedReceiptFields {
            return ParsedReceiptFields(
                merchantName = "Cafe Central",
                transactionDate = LocalDate.of(2026, 6, 30),
                totalAmount = BigDecimal("12.80"),
                currency = fallbackCurrency ?: ReceiptCurrency.EUR
            )
        }
    }

    private class FakeReceiptRepository : ReceiptRepository {
        var savedRequest: SaveReceiptRequest? = null

        override fun observeReceipts(): Flow<List<Receipt>> = flowOf(emptyList())

        override suspend fun getReceipt(receiptId: String): Receipt? = null

        override suspend fun saveReceipt(request: SaveReceiptRequest): Receipt {
            savedRequest = request
            return Receipt(
                id = "saved-1",
                merchantName = request.draft.parsedFields.merchantName ?: "Cafe Central",
                transactionDate = request.draft.parsedFields.transactionDate ?: LocalDate.of(2026, 7, 1),
                totalAmount = request.draft.parsedFields.totalAmount ?: BigDecimal("12.80"),
                currency = request.draft.parsedFields.currency ?: ReceiptCurrency.EUR,
                image = request.draft.image,
                rawOcrText = request.draft.ocrResult?.cleanedText.orEmpty(),
                notes = request.draft.notes,
                createdAt = Instant.ofEpochMilli(1_000),
                updatedAt = Instant.ofEpochMilli(1_000)
            )
        }

        override suspend fun deleteReceipt(receiptId: String) = Unit
    }

    private class FakeSettingsRepository : SnapReceiptSettingsRepository {
        override fun observeSettings(): Flow<SnapReceiptSettings> = flowOf(SnapReceiptSettings())

        override suspend fun updateSettings(settings: SnapReceiptSettings) = Unit
    }
}

package com.pekomon.snapreceipt.feature.capture

import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.ReceiptOcrResult
import com.pekomon.snapreceipt.domain.model.ReceiptSource
import com.pekomon.snapreceipt.domain.ocr.ReceiptOcrEngine
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
            }
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
        assertNull(uiState.ocrErrorMessage)
        assertEquals(2, uiState.draft?.ocrResult?.lineBlocks?.size)
    }

    @Test
    fun importedImage_surfacesOcrFailure() = runTest(dispatcher) {
        val viewModel = CaptureViewModel(
            ocrEngine = object : ReceiptOcrEngine {
                override suspend fun extractText(image: ReceiptImage): ReceiptOcrResult {
                    error("OCR unavailable")
                }
            }
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
}

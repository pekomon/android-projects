package com.pekomon.snapreceipt.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pekomon.snapreceipt.domain.model.ParsedReceiptFields
import com.pekomon.snapreceipt.domain.model.Receipt
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.ReceiptDraft
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.ReceiptOcrResult
import com.pekomon.snapreceipt.domain.model.ReceiptSource
import com.pekomon.snapreceipt.domain.model.SnapReceiptSettings
import com.pekomon.snapreceipt.feature.capture.CaptureUiState
import com.pekomon.snapreceipt.feature.receipts.ReceiptsScreen
import com.pekomon.snapreceipt.feature.receipts.ReceiptsUiState
import com.pekomon.snapreceipt.feature.review.ReviewDraftFormState
import com.pekomon.snapreceipt.feature.review.ReviewScreen
import com.pekomon.snapreceipt.feature.settings.SettingsScreen
import com.pekomon.snapreceipt.feature.settings.SettingsUiState
import com.pekomon.snapreceipt.ui.theme.SnapReceiptTheme
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SnapReceiptScreenStateTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun receiptsScreen_showsEmptyStateWhenNoReceiptsExist() {
        composeRule.setContent {
            SnapReceiptTheme {
                ReceiptsScreen(
                    uiState = ReceiptsUiState(
                        receipts = emptyList(),
                        isLoading = false
                    ),
                    onReceiptSelected = {}
                )
            }
        }

        composeRule.onNodeWithText("No saved receipts yet").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Save a reviewed draft from Capture and it will appear here. This screen is now wired to the local Room repository instead of placeholder copy."
        ).assertIsDisplayed()
    }

    @Test
    fun receiptsScreen_showsSavedReceiptAndInvokesSelection() {
        var selectedReceiptId: String? = null
        val receipt = sampleReceipt()

        composeRule.setContent {
            SnapReceiptTheme {
                ReceiptsScreen(
                    uiState = ReceiptsUiState(
                        receipts = listOf(receipt),
                        isLoading = false
                    ),
                    onReceiptSelected = { selectedReceiptId = it }
                )
            }
        }

        composeRule.onNodeWithText("Northwind Cafe").assertIsDisplayed()
        composeRule.onNodeWithText("EUR 18.40").assertIsDisplayed()
        composeRule.onNodeWithText("Open").performClick()

        assertEquals(receipt.id, selectedReceiptId)
    }

    @Test
    fun reviewScreen_showsEmptyStateWhenNoDraftExists() {
        composeRule.setContent {
            SnapReceiptTheme {
                ReviewScreen(
                    uiState = CaptureUiState(),
                    onBackToCapture = {},
                    onSaveDraft = {},
                    onMerchantNameChange = {},
                    onTransactionDateChange = {},
                    onTotalAmountChange = {},
                    onCurrencyCodeChange = {},
                    onNotesChange = {}
                )
            }
        }

        composeRule.onNodeWithText("No draft to review yet.").assertIsDisplayed()
        composeRule.onNodeWithText("Open capture").assertIsDisplayed()
    }

    @Test
    fun reviewScreen_showsSettingsAwareContentForReadyDraft() {
        composeRule.setContent {
            SnapReceiptTheme {
                ReviewScreen(
                    uiState = CaptureUiState(
                        draft = sampleDraft(),
                        reviewForm = ReviewDraftFormState(
                            merchantName = "Northwind Cafe",
                            transactionDate = "05/07/2026",
                            totalAmount = "18.40",
                            currencyCode = "EUR",
                            notes = "Lunch meeting",
                            rawOcrPreview = "Northwind Cafe\n18.40 EUR"
                        ),
                        settings = SnapReceiptSettings(
                            defaultCurrency = ReceiptCurrency.USD,
                            imageCompressionQuality = 68
                        )
                    ),
                    onBackToCapture = {},
                    onSaveDraft = {},
                    onMerchantNameChange = {},
                    onTransactionDateChange = {},
                    onTotalAmountChange = {},
                    onCurrencyCodeChange = {},
                    onNotesChange = {}
                )
            }
        }

        composeRule.onNodeWithText("Review parsed receipt fields before save.").assertIsDisplayed()
        composeRule.onNodeWithText("Default currency fallback: USD. Saved JPG quality: 68%.").assertIsDisplayed()
        composeRule.onNodeWithText("Save receipt locally").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsPersistedDefaults() {
        composeRule.setContent {
            SnapReceiptTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        settings = SnapReceiptSettings(
                            defaultCurrency = ReceiptCurrency.GBP,
                            imageCompressionQuality = 73
                        ),
                        savedReceiptCount = 3,
                        isLoading = false
                    ),
                    onDefaultCurrencySelected = {},
                    onImageCompressionQualityChange = {}
                )
            }
        }

        composeRule.onNodeWithText("Local defaults").assertIsDisplayed()
        composeRule.onNodeWithText("73%").assertIsDisplayed()
        composeRule.onNodeWithText("3 saved receipt(s)").assertIsDisplayed()
        composeRule.onNodeWithText("GBP").assertIsDisplayed()
    }

    private fun sampleDraft() = ReceiptDraft(
        image = ReceiptImage(
            localPath = "file:///receipt.jpg",
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

    private fun sampleReceipt() = Receipt(
        id = "receipt-1",
        merchantName = "Northwind Cafe",
        transactionDate = LocalDate.of(2026, 7, 5),
        totalAmount = BigDecimal("18.40"),
        currency = ReceiptCurrency.EUR,
        image = ReceiptImage(
            localPath = "file:///receipt.jpg",
            source = ReceiptSource.CAMERA,
            mimeType = "image/jpeg"
        ),
        rawOcrText = "Northwind Cafe\n18.40 EUR",
        notes = "Lunch meeting",
        createdAt = Instant.ofEpochMilli(1_000),
        updatedAt = Instant.ofEpochMilli(1_000)
    )
}

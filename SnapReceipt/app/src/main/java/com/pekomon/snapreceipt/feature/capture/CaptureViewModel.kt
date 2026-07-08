package com.pekomon.snapreceipt.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pekomon.snapreceipt.core.demo.SnapReceiptDemoDataService
import com.pekomon.snapreceipt.domain.model.ParsedReceiptFields
import com.pekomon.snapreceipt.domain.model.ReceiptDraft
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.ReceiptSource
import com.pekomon.snapreceipt.domain.model.SaveReceiptRequest
import com.pekomon.snapreceipt.domain.model.SnapReceiptSettings
import com.pekomon.snapreceipt.domain.ocr.ReceiptOcrEngine
import com.pekomon.snapreceipt.domain.parsing.ReceiptParser
import com.pekomon.snapreceipt.domain.repository.ReceiptRepository
import com.pekomon.snapreceipt.domain.repository.SnapReceiptSettingsRepository
import com.pekomon.snapreceipt.feature.review.ReviewDraftFormState
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaptureViewModel(
    private val ocrEngine: ReceiptOcrEngine,
    private val receiptParser: ReceiptParser,
    private val receiptRepository: ReceiptRepository,
    private val settingsRepository: SnapReceiptSettingsRepository,
    private val demoDataService: SnapReceiptDemoDataService? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()
    private var currentSettings = SnapReceiptSettings()

    init {
        viewModelScope.launch {
            settingsRepository.observeSettings().collect { settings ->
                currentSettings = settings
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
    }

    fun onImageImported(
        uriString: String,
        source: ReceiptSource,
        mimeType: String?
    ) {
        val image = ReceiptImage(
            localPath = uriString,
            source = source,
            mimeType = mimeType
        )
        _uiState.value = CaptureUiState(
            selectedImage = image,
            isRunningOcr = true,
            settings = currentSettings
        )

        viewModelScope.launch {
            runCatching {
                ocrEngine.extractText(image)
            }.onSuccess { ocrResult ->
                val parsedFields = receiptParser.parse(
                    ocrResult = ocrResult,
                    fallbackCurrency = currentSettings.defaultCurrency
                )
                val draft = ReceiptDraft(
                    image = image,
                    ocrResult = ocrResult,
                    parsedFields = parsedFields
                )
                _uiState.value = CaptureUiState(
                    selectedImage = image,
                    draft = draft,
                    reviewForm = draft.toReviewForm(),
                    settings = currentSettings
                )
            }.onFailure { throwable ->
                _uiState.value = CaptureUiState(
                    selectedImage = image,
                    ocrErrorMessage = throwable.message ?: "Unable to read text from the selected receipt.",
                    settings = currentSettings
                )
            }
        }
    }

    fun clearImportedImage() {
        _uiState.value = CaptureUiState(
            lastSavedReceipt = _uiState.value.lastSavedReceipt,
            settings = currentSettings
        )
    }

    fun updateMerchantName(value: String) {
        updateReviewForm { it.copy(merchantName = value) }
    }

    fun updateTransactionDate(value: String) {
        updateReviewForm { it.copy(transactionDate = value) }
    }

    fun updateTotalAmount(value: String) {
        updateReviewForm { it.copy(totalAmount = value) }
    }

    fun updateCurrencyCode(value: String) {
        updateReviewForm { it.copy(currencyCode = value.uppercase(Locale.US)) }
    }

    fun updateNotes(value: String) {
        updateReviewForm { it.copy(notes = value) }
    }

    fun saveReviewedReceipt() {
        val currentState = _uiState.value
        val draft = currentState.draft ?: return
        if (!draft.isReadyToSave) {
            _uiState.value = currentState.copy(
                saveErrorMessage = "Merchant, date, total, and currency must all be valid before saving."
            )
            return
        }

        _uiState.value = currentState.copy(
            isSaving = true,
            saveErrorMessage = null
        )

        viewModelScope.launch {
            runCatching {
                receiptRepository.saveReceipt(
                    SaveReceiptRequest(
                        draft = draft,
                        imageCompressionQuality = currentSettings.imageCompressionQuality
                    )
                )
            }.onSuccess { savedReceipt ->
                _uiState.value = CaptureUiState(
                    lastSavedReceipt = savedReceipt,
                    settings = currentSettings
                )
            }.onFailure { throwable ->
                _uiState.value = currentState.copy(
                    isSaving = false,
                    saveErrorMessage = throwable.message ?: "Unable to save the reviewed receipt locally."
                )
            }
        }
    }

    fun loadDemoDraft() {
        val service = demoDataService ?: return
        _uiState.value = _uiState.value.copy(
            isLoadingDemoDraft = true,
            ocrErrorMessage = null,
            saveErrorMessage = null
        )
        viewModelScope.launch {
            runCatching {
                service.createDeterministicReviewDraft()
            }.onSuccess { draft ->
                currentSettings = SnapReceiptDemoDataService.DEMO_SETTINGS
                _uiState.value = CaptureUiState(
                    selectedImage = draft.image,
                    draft = draft,
                    reviewForm = draft.toReviewForm(),
                    settings = currentSettings
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoadingDemoDraft = false,
                    ocrErrorMessage = throwable.message ?: "Unable to load deterministic demo draft."
                )
            }
        }
    }

    private fun updateReviewForm(transform: (ReviewDraftFormState) -> ReviewDraftFormState) {
        val currentState = _uiState.value
        val draft = currentState.draft ?: return
        val reviewForm = currentState.reviewForm ?: return
        val nextForm = transform(reviewForm)
        val nextDraft = draft.copy(
            parsedFields = ParsedReceiptFields(
                merchantName = nextForm.merchantName.trim().ifBlank { null },
                transactionDate = parseDateText(nextForm.transactionDate),
                totalAmount = parseAmountText(nextForm.totalAmount),
                currency = ReceiptCurrency.fromCode(nextForm.currencyCode)
            ),
            notes = nextForm.notes
        )
        _uiState.value = currentState.copy(
            draft = nextDraft,
            reviewForm = nextForm,
            saveErrorMessage = null
        )
    }

    private fun ReceiptDraft.toReviewForm(): ReviewDraftFormState {
        return ReviewDraftFormState(
            merchantName = parsedFields.merchantName.orEmpty(),
            transactionDate = parsedFields.transactionDate?.format(REVIEW_DATE_FORMATTER).orEmpty(),
            totalAmount = parsedFields.totalAmount?.stripTrailingZeros()?.toPlainString().orEmpty(),
            currencyCode = parsedFields.currency?.code.orEmpty(),
            notes = notes,
            rawOcrPreview = ocrResult?.cleanedText.orEmpty()
        )
    }

    private fun parseDateText(value: String): LocalDate? {
        if (value.isBlank()) return null
        return try {
            LocalDate.parse(value.trim(), REVIEW_DATE_FORMATTER)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun parseAmountText(value: String): BigDecimal? {
        return value.trim()
            .replace(',', '.')
            .takeIf { it.isNotBlank() }
            ?.toBigDecimalOrNull()
    }

    companion object {
        val REVIEW_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        fun factory(
            ocrEngine: ReceiptOcrEngine,
            receiptParser: ReceiptParser,
            receiptRepository: ReceiptRepository,
            settingsRepository: SnapReceiptSettingsRepository,
            demoDataService: SnapReceiptDemoDataService? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CaptureViewModel(
                    ocrEngine = ocrEngine,
                    receiptParser = receiptParser,
                    receiptRepository = receiptRepository,
                    settingsRepository = settingsRepository,
                    demoDataService = demoDataService
                ) as T
            }
        }
    }
}

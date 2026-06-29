package com.pekomon.snapreceipt.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pekomon.snapreceipt.domain.model.ReceiptDraft
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.ReceiptSource
import com.pekomon.snapreceipt.domain.ocr.ReceiptOcrEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaptureViewModel(
    private val ocrEngine: ReceiptOcrEngine
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

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
            isRunningOcr = true
        )

        viewModelScope.launch {
            runCatching {
                ocrEngine.extractText(image)
            }.onSuccess { ocrResult ->
                _uiState.value = CaptureUiState(
                    selectedImage = image,
                    draft = ReceiptDraft(
                        image = image,
                        ocrResult = ocrResult
                    )
                )
            }.onFailure { throwable ->
                _uiState.value = CaptureUiState(
                    selectedImage = image,
                    ocrErrorMessage = throwable.message ?: "Unable to read text from the selected receipt."
                )
            }
        }
    }

    fun clearImportedImage() {
        _uiState.value = CaptureUiState()
    }

    companion object {
        fun factory(
            ocrEngine: ReceiptOcrEngine
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CaptureViewModel(ocrEngine) as T
            }
        }
    }
}

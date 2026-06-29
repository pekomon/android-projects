package com.pekomon.snapreceipt.feature.capture

import com.pekomon.snapreceipt.domain.model.ReceiptDraft
import com.pekomon.snapreceipt.domain.model.ReceiptImage

data class CaptureUiState(
    val selectedImage: ReceiptImage? = null,
    val draft: ReceiptDraft? = null,
    val isRunningOcr: Boolean = false,
    val ocrErrorMessage: String? = null
)

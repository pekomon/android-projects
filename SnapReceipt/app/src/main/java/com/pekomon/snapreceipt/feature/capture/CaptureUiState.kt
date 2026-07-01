package com.pekomon.snapreceipt.feature.capture

import com.pekomon.snapreceipt.domain.model.ReceiptDraft
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.Receipt
import com.pekomon.snapreceipt.feature.review.ReviewDraftFormState

data class CaptureUiState(
    val selectedImage: ReceiptImage? = null,
    val draft: ReceiptDraft? = null,
    val isRunningOcr: Boolean = false,
    val ocrErrorMessage: String? = null,
    val reviewForm: ReviewDraftFormState? = null,
    val isSaving: Boolean = false,
    val saveErrorMessage: String? = null,
    val lastSavedReceipt: Receipt? = null
)

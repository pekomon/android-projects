package com.pekomon.snapreceipt.feature.detail

import com.pekomon.snapreceipt.domain.model.Receipt

data class ReceiptDetailUiState(
    val receipt: Receipt? = null,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val wasDeleted: Boolean = false,
    val errorMessage: String? = null
)

package com.pekomon.snapreceipt.feature.receipts

import com.pekomon.snapreceipt.domain.model.Receipt

data class ReceiptsUiState(
    val receipts: List<Receipt> = emptyList(),
    val isLoading: Boolean = true
)

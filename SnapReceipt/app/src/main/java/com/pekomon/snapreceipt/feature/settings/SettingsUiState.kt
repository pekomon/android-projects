package com.pekomon.snapreceipt.feature.settings

import com.pekomon.snapreceipt.domain.model.SnapReceiptSettings

data class SettingsUiState(
    val settings: SnapReceiptSettings = SnapReceiptSettings(),
    val savedReceiptCount: Int = 0,
    val isLoading: Boolean = true
)

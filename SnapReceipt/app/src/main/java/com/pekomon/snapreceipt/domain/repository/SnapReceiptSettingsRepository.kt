package com.pekomon.snapreceipt.domain.repository

import com.pekomon.snapreceipt.domain.model.SnapReceiptSettings
import kotlinx.coroutines.flow.Flow

interface SnapReceiptSettingsRepository {
    fun observeSettings(): Flow<SnapReceiptSettings>

    suspend fun updateSettings(settings: SnapReceiptSettings)
}

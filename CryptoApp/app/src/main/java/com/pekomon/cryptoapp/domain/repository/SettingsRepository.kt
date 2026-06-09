package com.pekomon.cryptoapp.domain.repository

import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val appSettings: Flow<AppSettings>

    suspend fun updateCurrency(currency: Currency)
    suspend fun updateSortOption(sortOption: SortOption)
}

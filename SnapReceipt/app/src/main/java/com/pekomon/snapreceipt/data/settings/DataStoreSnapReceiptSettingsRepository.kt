package com.pekomon.snapreceipt.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.SnapReceiptSettings
import com.pekomon.snapreceipt.domain.repository.SnapReceiptSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.snapReceiptDataStore: DataStore<Preferences> by preferencesDataStore(name = "snapreceipt_settings")

class DataStoreSnapReceiptSettingsRepository(
    private val context: Context
) : SnapReceiptSettingsRepository {
    override fun observeSettings(): Flow<SnapReceiptSettings> {
        return context.snapReceiptDataStore.data.map { preferences ->
            SnapReceiptSettings(
                defaultCurrency = ReceiptCurrency.fromCode(preferences[DEFAULT_CURRENCY_KEY]) ?: SnapReceiptSettings().defaultCurrency,
                imageCompressionQuality = preferences[IMAGE_COMPRESSION_QUALITY_KEY] ?: SnapReceiptSettings().imageCompressionQuality
            )
        }
    }

    override suspend fun updateSettings(settings: SnapReceiptSettings) {
        context.snapReceiptDataStore.edit { preferences ->
            preferences[DEFAULT_CURRENCY_KEY] = settings.defaultCurrency.code
            preferences[IMAGE_COMPRESSION_QUALITY_KEY] = settings.imageCompressionQuality.coerceIn(40, 100)
        }
    }

    companion object {
        private val DEFAULT_CURRENCY_KEY = stringPreferencesKey("default_currency")
        private val IMAGE_COMPRESSION_QUALITY_KEY = intPreferencesKey("image_compression_quality")
    }
}

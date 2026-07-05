package com.pekomon.snapreceipt.data.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.SnapReceiptSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataStoreSnapReceiptSettingsRepositoryTest {
    private lateinit var repository: DataStoreSnapReceiptSettingsRepository

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = DataStoreSnapReceiptSettingsRepository(context)
        repository.updateSettings(SnapReceiptSettings())
    }

    @Test
    fun updateSettings_persistsCurrencyAndCompressionQuality() = runBlocking {
        repository.updateSettings(
            SnapReceiptSettings(
                defaultCurrency = ReceiptCurrency.USD,
                imageCompressionQuality = 67
            )
        )

        val observed = repository.observeSettings().first()

        assertEquals(ReceiptCurrency.USD, observed.defaultCurrency)
        assertEquals(67, observed.imageCompressionQuality)
    }

    @Test
    fun updateSettings_clampsCompressionQualityIntoExpectedRange() = runBlocking {
        repository.updateSettings(
            SnapReceiptSettings(
                defaultCurrency = ReceiptCurrency.GBP,
                imageCompressionQuality = 5
            )
        )

        val observed = repository.observeSettings().first()

        assertEquals(ReceiptCurrency.GBP, observed.defaultCurrency)
        assertEquals(40, observed.imageCompressionQuality)
    }
}

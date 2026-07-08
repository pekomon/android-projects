package com.pekomon.snapreceipt.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pekomon.snapreceipt.core.demo.SnapReceiptDemoDataService
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.SnapReceiptSettings
import com.pekomon.snapreceipt.domain.repository.ReceiptRepository
import com.pekomon.snapreceipt.domain.repository.SnapReceiptSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SnapReceiptSettingsRepository,
    private val receiptRepository: ReceiptRepository,
    private val demoDataService: SnapReceiptDemoDataService? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.observeSettings(),
                receiptRepository.observeReceipts()
            ) { settings, receipts ->
                SettingsUiState(
                    settings = settings,
                    savedReceiptCount = receipts.size,
                    isLoading = false,
                    isSeedingDemoData = _uiState.value.isSeedingDemoData
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateDefaultCurrency(currency: ReceiptCurrency) {
        viewModelScope.launch {
            val current = _uiState.value.settings
            settingsRepository.updateSettings(current.copy(defaultCurrency = currency))
        }
    }

    fun updateImageCompressionQuality(quality: Int) {
        viewModelScope.launch {
            val current = _uiState.value.settings
            settingsRepository.updateSettings(
                current.copy(imageCompressionQuality = quality.coerceIn(40, 100))
            )
        }
    }

    fun seedDeterministicDemoData() {
        val service = demoDataService ?: return
        _uiState.value = _uiState.value.copy(isSeedingDemoData = true)
        viewModelScope.launch {
            runCatching {
                service.seedDeterministicDemoData()
            }.also {
                _uiState.value = _uiState.value.copy(isSeedingDemoData = false)
            }
        }
    }

    companion object {
        fun factory(
            settingsRepository: SnapReceiptSettingsRepository,
            receiptRepository: ReceiptRepository,
            demoDataService: SnapReceiptDemoDataService? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    settingsRepository = settingsRepository,
                    receiptRepository = receiptRepository,
                    demoDataService = demoDataService
                ) as T
            }
        }
    }
}

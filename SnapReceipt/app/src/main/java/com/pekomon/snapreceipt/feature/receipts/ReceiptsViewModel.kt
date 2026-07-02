package com.pekomon.snapreceipt.feature.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pekomon.snapreceipt.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceiptsViewModel(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReceiptsUiState())
    val uiState: StateFlow<ReceiptsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            receiptRepository.observeReceipts().collect { receipts ->
                _uiState.update {
                    it.copy(
                        receipts = receipts,
                        isLoading = false
                    )
                }
            }
        }
    }

    companion object {
        fun factory(
            receiptRepository: ReceiptRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReceiptsViewModel(receiptRepository) as T
            }
        }
    }
}

package com.pekomon.snapreceipt.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pekomon.snapreceipt.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReceiptDetailViewModel(
    private val receiptId: String,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReceiptDetailUiState())
    val uiState: StateFlow<ReceiptDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                receiptRepository.getReceipt(receiptId)
            }.onSuccess { receipt ->
                _uiState.value = ReceiptDetailUiState(
                    receipt = receipt,
                    isLoading = false,
                    errorMessage = if (receipt == null) "This receipt is no longer available." else null
                )
            }.onFailure { throwable ->
                _uiState.value = ReceiptDetailUiState(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Unable to load this receipt."
                )
            }
        }
    }

    fun deleteReceipt() {
        val receipt = _uiState.value.receipt ?: return
        _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                receiptRepository.deleteReceipt(receipt.id)
            }.onSuccess {
                _uiState.value = ReceiptDetailUiState(
                    wasDeleted = true,
                    isLoading = false
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = throwable.message ?: "Unable to delete this receipt."
                )
            }
        }
    }

    companion object {
        fun factory(
            receiptId: String,
            receiptRepository: ReceiptRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReceiptDetailViewModel(receiptId, receiptRepository) as T
            }
        }
    }
}

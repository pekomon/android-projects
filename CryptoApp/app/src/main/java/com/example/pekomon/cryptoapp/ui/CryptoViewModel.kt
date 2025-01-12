package com.example.pekomon.cryptoapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekomon.cryptoapp.data.CryptoRepository
import kotlinx.coroutines.launch

class CryptoViewModel : ViewModel() {
    private val repository = CryptoRepository()
    
    var prices by mutableStateOf<Map<String, Double>>(emptyMap())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    init {
        fetchPrices()
    }
    
    fun fetchPrices() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                prices = repository.getCryptoPrices()
            } catch (e: Exception) {
                error = "Error fetching prices: ${e.message}"
            }
            isLoading = false
        }
    }
} 
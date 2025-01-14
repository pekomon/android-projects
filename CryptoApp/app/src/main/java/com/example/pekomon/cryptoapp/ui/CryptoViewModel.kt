package com.example.pekomon.cryptoapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekomon.cryptoapp.data.CryptoRepository
import com.example.pekomon.cryptoapp.data.CryptoInfo
import kotlinx.coroutines.launch

class CryptoViewModel : ViewModel() {
    private val repository = CryptoRepository()
    
    var cryptos by mutableStateOf<List<CryptoInfo>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
        
    private var favorites by mutableStateOf<Set<String>>(emptySet())
    
    private val cryptoDetails = mapOf(
        "bitcoin" to Pair("Bitcoin", "BTC"),
        "ethereum" to Pair("Ethereum", "ETH"),
        "dogecoin" to Pair("Dogecoin", "DOGE")
    )
    
    init {
        // Fetch prices immediately when ViewModel is created
        viewModelScope.launch {
            try {
                val prices = repository.getCryptoPrices()
                cryptos = prices.map { (id, price) ->
                    val (name, symbol) = cryptoDetails[id] ?: Pair(id.capitalize(), id.uppercase())
                    CryptoInfo(id, name, symbol, price)
                }
            } catch (e: Exception) {
                error = "Error fetching prices: ${e.message}"
            }
        }
    }
    
    fun fetchPrices() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val prices = repository.getCryptoPrices()
                cryptos = prices.map { (id, price) ->
                    val (name, symbol) = cryptoDetails[id] ?: Pair(id.capitalize(), id.uppercase())
                    CryptoInfo(id, name, symbol, price)
                }
            } catch (e: Exception) {
                error = "Error fetching prices: ${e.message}"
            }
            isLoading = false
        }
    }
    
    fun toggleFavorite(cryptoId: String) {
        favorites = if (cryptoId in favorites) {
            favorites - cryptoId
        } else {
            favorites + cryptoId
        }
    }
    
    fun isFavorite(cryptoId: String): Boolean = cryptoId in favorites
} 
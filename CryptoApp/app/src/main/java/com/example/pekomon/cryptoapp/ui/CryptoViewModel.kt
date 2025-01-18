package com.example.pekomon.cryptoapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekomon.cryptoapp.data.CryptoRepository
import com.example.pekomon.cryptoapp.data.CryptoInfo
import com.example.pekomon.cryptoapp.data.SortOption
import com.example.pekomon.cryptoapp.data.Currency
import com.example.pekomon.cryptoapp.data.PreferencesRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class CryptoViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val repository = CryptoRepository()
    
    var cryptos by mutableStateOf<List<CryptoInfo>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
        
    var favorites by mutableStateOf<Set<String>>(emptySet())
        private set
    
    private val cryptoDetails = mapOf(
        "bitcoin" to Pair("Bitcoin", "BTC"),
        "ethereum" to Pair("Ethereum", "ETH"),
        "dogecoin" to Pair("Dogecoin", "DOGE")
    )
    
    var currentSortOption by mutableStateOf(SortOption.NAME_ASC)
        private set
    
    var selectedCurrency by mutableStateOf(Currency.EUR)
        private set
    
    init {
        viewModelScope.launch {
            // Lataa tallennetut asetukset
            preferencesRepository.selectedCurrency.collect { currency ->
                selectedCurrency = currency
            }
        }
        
        viewModelScope.launch {
            preferencesRepository.sortOption.collect { option ->
                currentSortOption = option
            }
        }
        
        viewModelScope.launch {
            preferencesRepository.favorites.collect { savedFavorites ->
                favorites = savedFavorites
            }
        }
        
        fetchPrices()
    }
    
    fun updateSortOption(option: SortOption) {
        viewModelScope.launch {
            preferencesRepository.updateSortOption(option)
            currentSortOption = option
        }
    }
    
    fun updateCurrency(currency: Currency) {
        viewModelScope.launch {
            preferencesRepository.updateSelectedCurrency(currency)
            selectedCurrency = currency
            fetchPrices()
        }
    }
    
    fun toggleFavorite(cryptoId: String) {
        val newFavorites = if (cryptoId in favorites) {
            favorites - cryptoId
        } else {
            favorites + cryptoId
        }
        viewModelScope.launch {
            preferencesRepository.updateFavorites(newFavorites)
            favorites = newFavorites
        }
    }
    
    val sortedCryptos: List<CryptoInfo>
        get() = when (currentSortOption) {
            SortOption.NAME_ASC -> cryptos.sortedBy { it.name }
            SortOption.NAME_DESC -> cryptos.sortedByDescending { it.name }
            SortOption.SYMBOL_ASC -> cryptos.sortedBy { it.symbol }
            SortOption.SYMBOL_DESC -> cryptos.sortedByDescending { it.symbol }
            SortOption.PRICE_ASC -> cryptos.sortedBy { it.price }
            SortOption.PRICE_DESC -> cryptos.sortedByDescending { it.price }
        }
    
    fun fetchPrices() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val prices = repository.getCryptoPrices(selectedCurrency.code)
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
    
    fun isFavorite(cryptoId: String): Boolean = cryptoId in favorites
} 
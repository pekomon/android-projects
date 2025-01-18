package com.example.pekomon.cryptoapp.ui

import android.util.Log
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
import com.example.pekomon.cryptoapp.data.CryptoListItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

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
    
    var availableCryptos by mutableStateOf<List<CryptoListItem>>(emptyList())
        private set
        
    var selectedCryptos by mutableStateOf<Set<String>>(emptySet())
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
    
    private var isInitialized = false
    
    private val defaultCryptos = listOf(
        "bitcoin",
        "ethereum",
        "tether",
        "binancecoin",
        "ripple",
        "solana",
        "cardano",
        "dogecoin",
        "polkadot",
        "avalanche-2",
        "tron",
        "chainlink",
        "polygon",
        "litecoin",
        "bitcoin-cash",
        "stellar",
        "monero",
        "cosmos",
        "ethereum-classic",
        "hedera"
    )
    
    suspend fun initialize() {
        if (isInitialized) return
        
        try {
            isLoading = true
            error = null
            
            // Lataa asetukset
            selectedCurrency = preferencesRepository.selectedCurrency.first()
            currentSortOption = preferencesRepository.sortOption.first()
            favorites = preferencesRepository.favorites.first()
            
            // Lataa ensin saatavilla olevat kryptot
            loadAvailableCryptos()

            // Lataa valitut kryptot tai käytä oletuslistaa
            selectedCryptos = preferencesRepository.selectedCryptos.first().let { saved ->
                saved.ifEmpty {
                    defaultCryptos.toSet().also { defaultSelection ->
                        preferencesRepository.updateSelectedCryptos(defaultSelection)
                    }
                }
            }
            
            // Lataa hinnat valituille kryptoille
            fetchPrices()
            
            isInitialized = true
        } catch (e: Exception) {
            error = "Error initializing data: ${e.message}"
        } finally {
            isLoading = false
        }
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
                // Haetaan hinnat valituille ja suosikeille
                val cryptosToFetch = (selectedCryptos + favorites).toList()
                val prices = repository.getCryptoPrices(cryptosToFetch, selectedCurrency.code)
                
                cryptos = prices.map { (id, price) ->
                    val crypto = availableCryptos.find { it.id == id }
                    CryptoInfo(
                        id = id,
                        name = crypto?.name ?: id.capitalize(),
                        symbol = crypto?.symbol?.uppercase() ?: id.uppercase(),
                        price = price
                    )
                }
            } catch (e: Exception) {
                error = "Error fetching prices: ${e.message}"
            }
            isLoading = false
        }
    }
    
    fun isFavorite(cryptoId: String): Boolean = cryptoId in favorites
    
    private suspend fun loadAvailableCryptos() {
        try {
            availableCryptos = repository.getAllAvailableCryptos()
            Log.d("CryptoViewModel", "Loaded ${availableCryptos.size} available cryptocurrencies")
        } catch (e: Exception) {
            error = "Error loading available cryptocurrencies: ${e.message}"
            throw e  // Heitetään poikkeus eteenpäin, jotta initialize() käsittelee sen
        }
    }
    
    fun updateSelectedCryptos(cryptos: Set<String>) {
        viewModelScope.launch {
            preferencesRepository.updateSelectedCryptos(cryptos)
            selectedCryptos = cryptos
            fetchPrices()
        }
    }
} 
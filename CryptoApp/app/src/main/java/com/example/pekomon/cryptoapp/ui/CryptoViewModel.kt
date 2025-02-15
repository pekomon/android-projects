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
import com.example.pekomon.cryptoapp.data.UserCrypto
import com.example.pekomon.cryptoapp.data.Transaction
import com.example.pekomon.cryptoapp.data.TransactionType
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

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
    
    var currentSortOption by mutableStateOf(SortOption.DEFAULT)
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
    
    var userCryptos by mutableStateOf<List<UserCrypto>>(emptyList())
        private set
    
    val totalPortfolioValue: Double
        get() = userCryptos.sumOf { userCrypto ->
            val price = getCryptoInfo(userCrypto.cryptoId)?.currentPrice ?: 0.0
            price * userCrypto.amount
        }
    
    private var cryptoInfoMap = mutableMapOf<String, CryptoInfo>()
    
    init {
        viewModelScope.launch {
            preferencesRepository.userCryptos.collect { cryptos ->
                userCryptos = cryptos
            }
        }
    }
    
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
        currentSortOption = option
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
    
    val sortedCryptos: List<CryptoListItem>
        get() = when (currentSortOption) {
            SortOption.NAME_ASC -> availableCryptos.sortedBy { it.name }
            SortOption.NAME_DESC -> availableCryptos.sortedByDescending { it.name }
            SortOption.SYMBOL_ASC -> availableCryptos.sortedBy { it.symbol }
            SortOption.SYMBOL_DESC -> availableCryptos.sortedByDescending { it.symbol }
            SortOption.PRICE_ASC -> availableCryptos.sortedBy { getCryptoInfo(it.id)?.currentPrice ?: 0.0 }
            SortOption.PRICE_DESC -> availableCryptos.sortedByDescending { getCryptoInfo(it.id)?.currentPrice ?: 0.0 }
        }
    
    fun fetchPrices() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val cryptosToFetch = (selectedCryptos + favorites).toList()
                val prices = repository.getCryptoPrices(cryptosToFetch, selectedCurrency.code)
                
                cryptoInfoMap = prices.mapValues { (id, price) ->
                    CryptoInfo(
                        id = id,
                        currentPrice = price,
                        priceChangePercentage = 0.0  // Tämä pitäisi hakea API:sta
                    )
                }.toMutableMap()
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
    
    fun addUserCrypto(
        cryptoId: String,
        amount: Double,
        price: Double,
        dateTime: LocalDateTime
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                type = TransactionType.BUY,
                amount = amount,
                price = price,
                dateTime = dateTime
            )
            
            val newCrypto = UserCrypto(
                cryptoId = cryptoId,
                amount = amount,
                purchasePrice = price,
                purchaseDateTime = dateTime,
                transactions = listOf(transaction)
            )
            
            val newCryptos = userCryptos + newCrypto
            preferencesRepository.updateUserCryptos(newCryptos)
        }
    }
    
    fun updateUserCrypto(cryptoId: String, amount: Double) {
        viewModelScope.launch {
            val existingCrypto = userCryptos.find { it.cryptoId == cryptoId }
            if (existingCrypto != null) {
                val newCryptos = userCryptos.map { 
                    if (it.cryptoId == cryptoId) {
                        it.copy(amount = amount)
                    } else it 
                }
                preferencesRepository.updateUserCryptos(newCryptos)
            }
        }
    }
    
    fun removeUserCrypto(cryptoId: String) {
        viewModelScope.launch {
            val newCryptos = userCryptos.filter { it.cryptoId != cryptoId }
            preferencesRepository.updateUserCryptos(newCryptos)
        }
    }
    
    fun getCryptoInfo(id: String): CryptoInfo? = cryptoInfoMap[id]
} 
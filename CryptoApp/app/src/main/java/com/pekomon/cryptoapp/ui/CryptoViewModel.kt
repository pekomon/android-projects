package com.pekomon.cryptoapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pekomon.cryptoapp.data.CryptoRepository
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.PreferencesRepository
import com.pekomon.cryptoapp.data.PortfolioCalculator
import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.data.Transaction
import com.pekomon.cryptoapp.data.TransactionType
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.model.MarketPrice
import com.pekomon.cryptoapp.domain.repository.MarketRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class CryptoViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val repository: MarketRepository = CryptoRepository()
) : ViewModel() {
    var cryptos by mutableStateOf<List<MarketPrice>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set

    var marketLoadState by mutableStateOf<MarketLoadState>(MarketLoadState.Idle)
        private set
        
    var favorites by mutableStateOf<Set<String>>(emptySet())
        private set
    
    var availableCryptos by mutableStateOf<List<CryptoAsset>>(emptyList())
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
        get() = PortfolioCalculator.totalValue(userCryptos) { cryptoId ->
            getCryptoInfo(cryptoId)?.currentPrice
        }
    
    private var cryptoInfoMap = mutableMapOf<String, MarketPrice>()
    
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
    
    val sortedCryptos: List<CryptoAsset>
        get() = sortCryptos(availableCryptos.filter { it.id in selectedCryptos })

    val sortedFavoriteCryptos: List<CryptoAsset>
        get() = sortCryptos(availableCryptos.filter { it.id in favorites })

    private fun sortCryptos(cryptos: List<CryptoAsset>): List<CryptoAsset> {
        return when (currentSortOption) {
            SortOption.NAME_ASC -> cryptos.sortedBy { it.name }
            SortOption.NAME_DESC -> cryptos.sortedByDescending { it.name }
            SortOption.SYMBOL_ASC -> cryptos.sortedBy { it.symbol }
            SortOption.SYMBOL_DESC -> cryptos.sortedByDescending { it.symbol }
            SortOption.PRICE_ASC -> cryptos.sortedBy { getCryptoInfo(it.id)?.currentPrice ?: 0.0 }
            SortOption.PRICE_DESC -> cryptos.sortedByDescending { getCryptoInfo(it.id)?.currentPrice ?: 0.0 }
        }
    }
    
    fun fetchPrices() {
        viewModelScope.launch {
            isLoading = true
            error = null
            marketLoadState = MarketLoadState.Loading
            try {
                val cryptosToFetch = (selectedCryptos + favorites + userCryptos.map { it.cryptoId }).toList()
                val prices = repository.getCryptoPrices(cryptosToFetch, selectedCurrency.code)
                
                cryptoInfoMap = prices.mapValues { (id, price) ->
                    MarketPrice(
                        cryptoId = id,
                        currentPrice = price,
                        priceChangePercentage = 0.0
                    )
                }.toMutableMap()
                marketLoadState = MarketLoadState.Content(lastUpdated = LocalDateTime.now())
            } catch (e: Exception) {
                error = "Unable to load prices. Check your connection and try again."
                marketLoadState = MarketLoadState.Error(error ?: "Unable to load prices.")
            }
            isLoading = false
        }
    }
    
    fun isFavorite(cryptoId: String): Boolean = cryptoId in favorites
    
    private suspend fun loadAvailableCryptos() {
        try {
            availableCryptos = repository.getAllAvailableCryptos()
        } catch (e: Exception) {
            error = "Unable to load available cryptocurrencies. Check your connection and try again."
            throw e
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
            fetchPrices()
        }
    }
    
    fun updateUserCrypto(cryptoId: String, amount: Double) {
        updateUserCrypto(cryptoId = cryptoId, amount = amount, price = 0.0, dateTime = LocalDateTime.now())
    }

    fun updateUserCrypto(
        cryptoId: String,
        amount: Double,
        price: Double,
        dateTime: LocalDateTime
    ) {
        viewModelScope.launch {
            val existingCrypto = getCombinedUserCryptos().find { it.cryptoId == cryptoId }
            if (existingCrypto != null) {
                val adjustment = amount - existingCrypto.amount
                val adjustmentTransaction = when {
                    adjustment > 0.0 -> Transaction(
                        type = TransactionType.BUY,
                        amount = adjustment,
                        price = price,
                        dateTime = dateTime
                    )
                    adjustment < 0.0 -> Transaction(
                        type = TransactionType.SELL,
                        amount = -adjustment,
                        price = price,
                        dateTime = dateTime
                    )
                    else -> null
                }
                val updatedCrypto = existingCrypto.copy(
                    amount = amount,
                    transactions = existingCrypto.transactions + listOfNotNull(adjustmentTransaction)
                )
                val newCryptos = userCryptos.filterNot { it.cryptoId == cryptoId }.let { remaining ->
                    if (amount > 0.0) remaining + updatedCrypto else remaining
                }
                preferencesRepository.updateUserCryptos(newCryptos)
                fetchPrices()
            }
        }
    }
    
    fun removeUserCrypto(cryptoId: String) {
        viewModelScope.launch {
            val newCryptos = userCryptos.filter { it.cryptoId != cryptoId }
            preferencesRepository.updateUserCryptos(newCryptos)
            fetchPrices()
        }
    }
    
    fun getCryptoInfo(id: String): MarketPrice? = cryptoInfoMap[id]
    
    fun getCombinedUserCryptos(): List<UserCrypto> {
        return PortfolioCalculator.combineHoldings(userCryptos)
    }
}

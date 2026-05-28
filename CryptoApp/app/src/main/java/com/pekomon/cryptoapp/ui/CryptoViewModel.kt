package com.pekomon.cryptoapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pekomon.cryptoapp.core.logging.CryptoAppLogger
import com.pekomon.cryptoapp.data.CryptoRepository
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.local.PreferencesRepository
import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.data.Transaction
import com.pekomon.cryptoapp.data.TransactionType
import com.pekomon.cryptoapp.domain.market.CryptoAssetSorter
import com.pekomon.cryptoapp.domain.market.CryptoSelectionSanitizer
import com.pekomon.cryptoapp.domain.market.DefaultCryptoAssets
import com.pekomon.cryptoapp.domain.market.MarketDataError
import com.pekomon.cryptoapp.domain.market.MarketDataResult
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.model.MarketPrice
import com.pekomon.cryptoapp.domain.portfolio.PortfolioCalculator
import com.pekomon.cryptoapp.domain.portfolio.PortfolioValidationResult
import com.pekomon.cryptoapp.domain.portfolio.PortfolioValidator
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

    var assetMetadataSource by mutableStateOf<AssetMetadataSource?>(null)
        private set
    
    var selectedCryptos by mutableStateOf<Set<String>>(emptySet())
        private set
    
    var currentSortOption by mutableStateOf(SortOption.DEFAULT)
        private set
    
    var selectedCurrency by mutableStateOf(Currency.EUR)
        private set
    
    private var isInitialized = false
    
    private val defaultCryptos = DefaultCryptoAssets.assets.map { it.id }
    
    var userCryptos by mutableStateOf<List<UserCrypto>>(emptyList())
        private set
    
    val totalPortfolioValue: Double
        get() = PortfolioCalculator.totalValue(userCryptos) { cryptoId ->
            getCryptoInfo(cryptoId)?.currentPrice
        }
    
    private var cryptoInfoMap = mutableMapOf<String, MarketPrice>()
    private var lastMarketUpdated: LocalDateTime? = null
    
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
            
            selectedCurrency = preferencesRepository.selectedCurrency.first()
            currentSortOption = preferencesRepository.sortOption.first()
            favorites = preferencesRepository.favorites.first()
            
            loadAvailableCryptos()

            selectedCryptos = sanitizeSelectedCryptos(preferencesRepository.selectedCryptos.first())
            
            fetchPrices()
            
            isInitialized = true
        } catch (e: Exception) {
            CryptoAppLogger.error(TAG, "initialize failed", e)
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
        return CryptoAssetSorter.sort(
            assets = cryptos,
            sortOption = currentSortOption,
            priceForAsset = ::getCryptoInfo
        )
    }
    
    fun fetchPrices() {
        viewModelScope.launch {
            isLoading = true
            error = null
            marketLoadState = MarketLoadState.Loading
            try {
                val cryptosToFetch = (selectedCryptos + favorites + userCryptos.map { it.cryptoId }).toList()
                CryptoAppLogger.debug(TAG, "fetchPrices start ids=${cryptosToFetch.distinct().joinToString(",")} currency=${selectedCurrency.code}")
                when (val result = repository.getCryptoPricesResult(cryptosToFetch, selectedCurrency.code)) {
                    is MarketDataResult.Success -> {
                        cryptoInfoMap = result.value.toMarketPriceMap()
                        CryptoAppLogger.debug(TAG, "fetchPrices success count=${cryptoInfoMap.size}")
                        lastMarketUpdated = LocalDateTime.now()
                        marketLoadState = MarketLoadState.Content(lastUpdated = lastMarketUpdated ?: LocalDateTime.now())
                    }
                    is MarketDataResult.PartialSuccess -> {
                        val fetchedPrices = result.value.toMarketPriceMap()
                        cryptoInfoMap = cryptoInfoMap.toMutableMap().apply {
                            putAll(fetchedPrices)
                        }
                        CryptoAppLogger.warning(
                            TAG,
                            "fetchPrices partial success count=${fetchedPrices.size} missingIds=${result.missingIds.joinToString(",")}"
                        )
                        lastMarketUpdated = LocalDateTime.now()
                        marketLoadState = MarketLoadState.Content(
                            lastUpdated = lastMarketUpdated ?: LocalDateTime.now(),
                            isStale = result.missingIds.any { it in cryptoInfoMap },
                            message = partialPriceMessage(result.missingIds)
                        )
                    }
                    is MarketDataResult.Failure -> {
                        val message = result.error.userMessage()
                        error = message
                        CryptoAppLogger.error(TAG, "fetchPrices failed ${result.error.technicalMessage}")
                        marketLoadState = if (cryptoInfoMap.isNotEmpty() && lastMarketUpdated != null) {
                            MarketLoadState.Content(
                                lastUpdated = lastMarketUpdated ?: LocalDateTime.now(),
                                isStale = true,
                                message = message
                            )
                        } else {
                            MarketLoadState.Error(message)
                        }
                    }
                }
            } catch (e: Exception) {
                CryptoAppLogger.error(TAG, "fetchPrices failed", e)
                error = MarketDataError.Unknown(e.message).userMessage()
                marketLoadState = if (cryptoInfoMap.isNotEmpty() && lastMarketUpdated != null) {
                    MarketLoadState.Content(
                        lastUpdated = lastMarketUpdated ?: LocalDateTime.now(),
                        isStale = true,
                        message = error
                    )
                } else {
                    MarketLoadState.Error(error ?: "Unable to load prices.")
                }
            }
            isLoading = false
        }
    }
    
    fun isFavorite(cryptoId: String): Boolean = cryptoId in favorites
    
    private suspend fun loadAvailableCryptos() {
        try {
            CryptoAppLogger.debug(TAG, "loadAvailableCryptos using live API")
            val liveAssets = repository.getAllAvailableCryptos()
            availableCryptos = liveAssets
            assetMetadataSource = AssetMetadataSource.Live
            preferencesRepository.updateCachedCryptoAssets(liveAssets)
            CryptoAppLogger.debug(TAG, "loadAvailableCryptos live success count=${liveAssets.size}")
        } catch (e: Exception) {
            CryptoAppLogger.error(TAG, "loadAvailableCryptos live failed; trying cache", e)
            val cachedAssets = preferencesRepository.cachedCryptoAssets.first()
            availableCryptos = cachedAssets.ifEmpty { DefaultCryptoAssets.assets }
            assetMetadataSource = if (cachedAssets.isEmpty()) {
                AssetMetadataSource.Default
            } else {
                AssetMetadataSource.Cache
            }
            CryptoAppLogger.debug(
                TAG,
                "loadAvailableCryptos fallback source=${if (cachedAssets.isEmpty()) "default" else "cache"} count=${availableCryptos.size}"
            )
        }
    }
    
    fun updateSelectedCryptos(cryptos: Set<String>) {
        viewModelScope.launch {
            val sanitized = sanitizeSelectedCryptos(cryptos)
            preferencesRepository.updateSelectedCryptos(sanitized)
            selectedCryptos = sanitized
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
            val validation = PortfolioValidator.validateTransactionInput(amount, price)
            if (validation is PortfolioValidationResult.Invalid) {
                error = validation.message
                return@launch
            }

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
            
            val newCryptos = PortfolioCalculator.normalizeHoldings(userCryptos + newCrypto)
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
            val validation = PortfolioValidator.validateTransactionInput(amount, price)
            if (validation is PortfolioValidationResult.Invalid) {
                error = validation.message
                return@launch
            }

            val existingCrypto = getCombinedUserCryptos().find { it.cryptoId == cryptoId }
            if (existingCrypto == null) {
                error = "This holding is no longer in your portfolio."
                return@launch
            }

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

            if (adjustmentTransaction == null) {
                error = null
                return@launch
            }

            val updatedCrypto = existingCrypto.copy(
                transactions = existingCrypto.transactions + adjustmentTransaction
            )
            val newCryptos = PortfolioCalculator.normalizeHoldings(
                userCryptos.filterNot { it.cryptoId == cryptoId } + updatedCrypto
            )
            preferencesRepository.updateUserCryptos(newCryptos)
            fetchPrices()
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

    private fun MarketDataError.userMessage(): String = when (this) {
        is MarketDataError.Unauthorized -> {
            "CoinGecko rejected the API key. Check COINGECKO_DEMO_API_KEY in local.properties."
        }
        is MarketDataError.Forbidden -> {
            "CoinGecko access is forbidden. Check the API plan, key, or endpoint."
        }
        is MarketDataError.RateLimited -> {
            "CoinGecko is rate limiting requests. Wait a moment and refresh again."
        }
        is MarketDataError.Network -> {
            "Unable to reach CoinGecko. Check your connection and try again."
        }
        is MarketDataError.Unknown -> "Unable to load prices. Check your connection and try again."
    }

    private fun Map<String, Double>.toMarketPriceMap(): MutableMap<String, MarketPrice> {
        return mapValues { (id, price) ->
            MarketPrice(
                cryptoId = id,
                currentPrice = price,
                priceChangePercentage = 0.0
            )
        }.toMutableMap()
    }

    private fun partialPriceMessage(missingIds: Set<String>): String {
        return if (missingIds.size == 1) {
            "1 price could not be updated."
        } else {
            "${missingIds.size} prices could not be updated."
        }
    }

    private suspend fun sanitizeSelectedCryptos(savedSelection: Set<String>): Set<String> {
        val sanitized = CryptoSelectionSanitizer.sanitizeSelection(
            selectedIds = savedSelection,
            availableAssets = availableCryptos,
            fallbackIds = defaultCryptos.toSet()
        )

        if (sanitized != savedSelection) {
            CryptoAppLogger.debug(
                TAG,
                "sanitizeSelectedCryptos removed=${(savedSelection - sanitized).joinToString(",")} selected=${sanitized.joinToString(",")}"
            )
            preferencesRepository.updateSelectedCryptos(sanitized)
        }

        return sanitized
    }

    private companion object {
        const val TAG = "CryptoAppNetwork"
    }
}

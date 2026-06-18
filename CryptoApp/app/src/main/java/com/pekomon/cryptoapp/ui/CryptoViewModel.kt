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
import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.domain.market.DefaultCryptoAssets
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.model.MarketPrice
import com.pekomon.cryptoapp.domain.portfolio.PortfolioCalculator
import com.pekomon.cryptoapp.domain.repository.MarketRepository
import com.pekomon.cryptoapp.domain.repository.UserPreferencesRepository
import com.pekomon.cryptoapp.ui.state.FavoritesUiState
import com.pekomon.cryptoapp.ui.state.FavoritesStateOwner
import com.pekomon.cryptoapp.ui.state.PortfolioMutationResult
import com.pekomon.cryptoapp.ui.state.PortfolioStateOwner
import com.pekomon.cryptoapp.ui.state.PortfolioUiState
import com.pekomon.cryptoapp.ui.state.SettingsStateOwner
import com.pekomon.cryptoapp.ui.state.SettingsUiState
import com.pekomon.cryptoapp.ui.state.WatchlistStateOwner
import com.pekomon.cryptoapp.ui.state.WatchlistUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class CryptoViewModel(
    private val preferencesRepository: UserPreferencesRepository,
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
    private val watchlistStateOwner = WatchlistStateOwner()
    private val favoritesStateOwner = FavoritesStateOwner()
    private val portfolioStateOwner = PortfolioStateOwner()
    private val settingsStateOwner = SettingsStateOwner()
    private val marketDataCoordinator = MarketDataCoordinator(repository)
    
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
        val newSortOption = settingsStateOwner.updateSortOption(option)
        viewModelScope.launch {
            preferencesRepository.updateSortOption(newSortOption)
            currentSortOption = newSortOption
        }
    }
    
    fun updateCurrency(currency: Currency) {
        val newCurrency = settingsStateOwner.updateCurrency(currency)
        viewModelScope.launch {
            preferencesRepository.updateSelectedCurrency(newCurrency)
            selectedCurrency = newCurrency
            fetchPrices()
        }
    }
    
    fun toggleFavorite(cryptoId: String) {
        val newFavorites = favoritesStateOwner.toggleFavorite(
            favoriteIds = favorites,
            cryptoId = cryptoId
        )
        viewModelScope.launch {
            preferencesRepository.updateFavorites(newFavorites)
            favorites = newFavorites
        }
    }
    
    val sortedCryptos: List<CryptoAsset>
        get() = watchlistStateOwner.sortedAssets(
            availableAssets = availableCryptos,
            selectedAssetIds = selectedCryptos,
            sortOption = currentSortOption,
            priceForAsset = ::getCryptoInfo
        )

    val sortedFavoriteCryptos: List<CryptoAsset>
        get() = favoritesStateOwner.sortedAssets(
            availableAssets = availableCryptos,
            favoriteIds = favorites,
            sortOption = currentSortOption,
            priceForAsset = ::getCryptoInfo
        )

    val watchlistUiState: WatchlistUiState
        get() = watchlistStateOwner.state(
            availableAssets = availableCryptos,
            selectedAssetIds = selectedCryptos,
            favoriteIds = favorites,
            prices = cryptoInfoMap,
            selectedCurrency = selectedCurrency,
            sortOption = currentSortOption,
            marketLoadState = marketLoadState,
            isLoading = isLoading,
            errorMessage = error,
            assetMetadataSource = assetMetadataSource
        )

    val favoritesUiState: FavoritesUiState
        get() = favoritesStateOwner.state(
            availableAssets = availableCryptos,
            favoriteIds = favorites,
            prices = cryptoInfoMap,
            selectedCurrency = selectedCurrency,
            sortOption = currentSortOption,
            marketLoadState = marketLoadState,
            isLoading = isLoading,
            errorMessage = error
        )

    val portfolioUiState: PortfolioUiState
        get() = portfolioStateOwner.state(
            userCryptos = userCryptos,
            prices = cryptoInfoMap,
            selectedCurrency = selectedCurrency,
            marketLoadState = marketLoadState,
            isLoading = isLoading,
            errorMessage = error
        )

    val settingsUiState: SettingsUiState
        get() = settingsStateOwner.state(
            availableAssets = availableCryptos,
            selectedAssetIds = selectedCryptos,
            favoriteIds = favorites,
            selectedCurrency = selectedCurrency,
            sortOption = currentSortOption,
            assetMetadataSource = assetMetadataSource,
            isLoading = isLoading,
            errorMessage = error
        )

    fun fetchPrices() {
        viewModelScope.launch {
            isLoading = true
            error = null
            marketLoadState = MarketLoadState.Loading
            val result = marketDataCoordinator.fetchPrices(
                cryptoIds = (selectedCryptos + favorites + userCryptos.map { it.cryptoId }).toList(),
                currencyCode = selectedCurrency.code,
                currentPrices = cryptoInfoMap,
                lastUpdated = lastMarketUpdated
            )
            cryptoInfoMap = result.prices.toMutableMap()
            lastMarketUpdated = result.lastUpdated
            marketLoadState = result.marketLoadState
            error = result.errorMessage
            isLoading = false
        }
    }
    
    fun isFavorite(cryptoId: String): Boolean {
        return favoritesStateOwner.isFavorite(
            favoriteIds = favorites,
            cryptoId = cryptoId
        )
    }
    
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
            when (val result = portfolioStateOwner.addHolding(
                currentHoldings = userCryptos,
                cryptoId = cryptoId,
                amount = amount,
                price = price,
                dateTime = dateTime
            )) {
                is PortfolioMutationResult.Success -> {
                    preferencesRepository.updateUserCryptos(result.holdings)
                    fetchPrices()
                }
                is PortfolioMutationResult.Failure -> error = result.message
                PortfolioMutationResult.NoChange -> error = null
            }
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
            when (val result = portfolioStateOwner.updateHolding(
                currentHoldings = userCryptos,
                cryptoId = cryptoId,
                amount = amount,
                price = price,
                dateTime = dateTime
            )) {
                is PortfolioMutationResult.Success -> {
                    preferencesRepository.updateUserCryptos(result.holdings)
                    fetchPrices()
                }
                is PortfolioMutationResult.Failure -> error = result.message
                PortfolioMutationResult.NoChange -> error = null
            }
        }
    }
    
    fun removeUserCrypto(cryptoId: String) {
        viewModelScope.launch {
            val newCryptos = portfolioStateOwner.removeHolding(
                currentHoldings = userCryptos,
                cryptoId = cryptoId
            )
            preferencesRepository.updateUserCryptos(newCryptos)
            fetchPrices()
        }
    }
    
    fun getCryptoInfo(id: String): MarketPrice? = cryptoInfoMap[id]
    
    fun getCombinedUserCryptos(): List<UserCrypto> {
        return PortfolioCalculator.combineHoldings(userCryptos)
    }

    private suspend fun sanitizeSelectedCryptos(savedSelection: Set<String>): Set<String> {
        val sanitized = watchlistStateOwner.sanitizeSelectedAssetIds(
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

package com.pekomon.cryptoapp.ui

import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.data.TransactionType
import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.domain.market.MarketDataError
import com.pekomon.cryptoapp.domain.market.MarketDataResult
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.repository.MarketRepository
import com.pekomon.cryptoapp.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class CryptoViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initializeSanitizesSavedSelectionAndLoadsPrices() = runViewModelTest {
        val preferences = FakePreferencesRepository(
            selectedCryptos = setOf("bitcoin", "missing"),
            favorites = setOf("ethereum")
        )
        val market = FakeMarketRepository()
        val viewModel = CryptoViewModel(preferences, market)

        viewModel.initialize()
        advanceUntilIdle()

        assertEquals(setOf("bitcoin"), viewModel.selectedCryptos)
        assertEquals(setOf("bitcoin"), preferences.selectedCryptos.value)
        assertEquals(60_000.0, viewModel.getCryptoInfo("bitcoin")?.currentPrice ?: 0.0, 0.0)
        assertEquals(3_000.0, viewModel.getCryptoInfo("ethereum")?.currentPrice ?: 0.0, 0.0)
        assertTrue(viewModel.marketLoadState is MarketLoadState.Content)
    }

    @Test
    fun updateCurrencyPersistsSelectionAndRefreshesMarketData() = runViewModelTest {
        val preferences = FakePreferencesRepository(selectedCryptos = setOf("bitcoin"))
        val market = FakeMarketRepository()
        val viewModel = CryptoViewModel(preferences, market)
        viewModel.initialize()
        advanceUntilIdle()

        viewModel.updateCurrency(Currency.USD)
        advanceUntilIdle()

        assertEquals(Currency.USD, viewModel.selectedCurrency)
        assertEquals(Currency.USD, preferences.selectedCurrency.value)
        assertEquals("usd", market.priceRequests.last().currency)
    }

    @Test
    fun toggleFavoritePersistsNewFavoriteSet() = runViewModelTest {
        val preferences = FakePreferencesRepository(favorites = setOf("bitcoin"))
        val viewModel = CryptoViewModel(preferences, FakeMarketRepository())
        viewModel.initialize()
        advanceUntilIdle()

        viewModel.toggleFavorite("bitcoin")
        advanceUntilIdle()
        assertFalse(viewModel.isFavorite("bitcoin"))
        assertEquals(emptySet<String>(), preferences.favorites.value)

        viewModel.toggleFavorite("ethereum")
        advanceUntilIdle()
        assertTrue(viewModel.isFavorite("ethereum"))
        assertEquals(setOf("ethereum"), preferences.favorites.value)
    }

    @Test
    fun portfolioAddUpdateAndRemovePersistTransactionBackedHoldings() = runViewModelTest {
        val preferences = FakePreferencesRepository(selectedCryptos = setOf("bitcoin"))
        val viewModel = CryptoViewModel(preferences, FakeMarketRepository())
        val firstBuy = LocalDateTime.of(2026, 6, 1, 10, 0)
        val secondTrade = LocalDateTime.of(2026, 6, 2, 10, 0)
        viewModel.initialize()
        advanceUntilIdle()

        viewModel.addUserCrypto("bitcoin", amount = 2.0, price = 100.0, dateTime = firstBuy)
        advanceUntilIdle()

        assertEquals(2.0, viewModel.getCombinedUserCryptos().single().amount, 0.0)
        assertEquals(TransactionType.BUY, preferences.userCryptos.value.single().transactions.single().type)

        viewModel.updateUserCrypto("bitcoin", amount = 1.0, price = 120.0, dateTime = secondTrade)
        advanceUntilIdle()

        val updatedHolding = viewModel.getCombinedUserCryptos().single()
        assertEquals(1.0, updatedHolding.amount, 0.0)
        assertEquals(listOf(TransactionType.BUY, TransactionType.SELL), updatedHolding.transactions.map { it.type })

        viewModel.removeUserCrypto("bitcoin")
        advanceUntilIdle()

        assertTrue(viewModel.getCombinedUserCryptos().isEmpty())
        assertTrue(preferences.userCryptos.value.isEmpty())
    }

    @Test
    fun fetchFailureWithoutCachedPricesExposesErrorState() = runViewModelTest {
        val preferences = FakePreferencesRepository(selectedCryptos = setOf("bitcoin"))
        val market = FakeMarketRepository(
            priceResult = MarketDataResult.Failure(MarketDataError.Network("offline"))
        )
        val viewModel = CryptoViewModel(preferences, market)

        viewModel.initialize()
        advanceUntilIdle()

        assertTrue(viewModel.marketLoadState is MarketLoadState.Error)
        assertNotNull(viewModel.error)
    }

    private fun runViewModelTest(block: suspend kotlinx.coroutines.test.TestScope.() -> Unit) {
        runTest(dispatcher) {
            block()
        }
    }
}

private class FakePreferencesRepository(
    selectedCurrency: Currency = Currency.EUR,
    sortOption: SortOption = SortOption.DEFAULT,
    favorites: Set<String> = emptySet(),
    selectedCryptos: Set<String> = emptySet(),
    userCryptos: List<UserCrypto> = emptyList(),
    cachedCryptoAssets: List<CryptoAsset> = emptyList()
) : UserPreferencesRepository {
    override val selectedCurrency = MutableStateFlow(selectedCurrency)
    override val sortOption = MutableStateFlow(sortOption)
    override val favorites = MutableStateFlow(favorites)
    override val selectedCryptos = MutableStateFlow(selectedCryptos)
    override val userCryptos = MutableStateFlow(userCryptos)
    override val cachedCryptoAssets = MutableStateFlow(cachedCryptoAssets)

    override suspend fun updateSelectedCurrency(currency: Currency) {
        selectedCurrency.value = currency
    }

    override suspend fun updateSortOption(sortOption: SortOption) {
        this.sortOption.value = sortOption
    }

    override suspend fun updateFavorites(favorites: Set<String>) {
        this.favorites.value = favorites
    }

    override suspend fun updateSelectedCryptos(cryptos: Set<String>) {
        selectedCryptos.value = cryptos
    }

    override suspend fun updateUserCryptos(cryptos: List<UserCrypto>) {
        userCryptos.value = cryptos
    }

    override suspend fun updateCachedCryptoAssets(assets: List<CryptoAsset>) {
        cachedCryptoAssets.value = assets
    }
}

private class FakeMarketRepository(
    private val assets: List<CryptoAsset> = listOf(
        CryptoAsset("bitcoin", "btc", "Bitcoin", 1),
        CryptoAsset("ethereum", "eth", "Ethereum", 2)
    ),
    private val priceResult: MarketDataResult<Map<String, Double>> = MarketDataResult.Success(
        mapOf(
            "bitcoin" to 60_000.0,
            "ethereum" to 3_000.0
        )
    )
) : MarketRepository {
    val priceRequests = mutableListOf<PriceRequest>()

    override suspend fun getAllAvailableCryptos(): List<CryptoAsset> = assets

    override suspend fun getCryptoPrices(
        coinIds: List<String>,
        currency: String
    ): Map<String, Double> {
        val result = getCryptoPricesResult(coinIds, currency)
        return when (result) {
            is MarketDataResult.Success -> result.value
            is MarketDataResult.PartialSuccess -> result.value
            is MarketDataResult.Failure -> emptyMap()
        }
    }

    override suspend fun getCryptoPricesResult(
        coinIds: List<String>,
        currency: String
    ): MarketDataResult<Map<String, Double>> {
        priceRequests += PriceRequest(coinIds = coinIds, currency = currency)
        return priceResult
    }
}

private data class PriceRequest(
    val coinIds: List<String>,
    val currency: String
)

package com.pekomon.cryptoapp.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.data.Transaction
import com.pekomon.cryptoapp.data.TransactionType
import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.domain.market.MarketDataError
import com.pekomon.cryptoapp.domain.market.MarketDataResult
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.domain.repository.MarketRepository
import com.pekomon.cryptoapp.domain.repository.UserPreferencesRepository
import com.pekomon.cryptoapp.ui.CryptoViewModel
import com.pekomon.cryptoapp.ui.testing.CryptoTestTags
import com.pekomon.cryptoapp.ui.theme.CryptoAppTheme
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class ScreenStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun watchlistShowsContentState() {
        val viewModel = initializedViewModel(
            preferences = FakePreferencesRepository(selectedCryptos = setOf("bitcoin")),
            market = FakeMarketRepository()
        )

        composeRule.setContent {
            CryptoAppTheme(darkTheme = false) {
                HomeScreen(viewModel = viewModel)
            }
        }

        composeRule.waitUntilExists(CryptoTestTags.WATCHLIST_LIST)
        composeRule.onNodeWithTag(CryptoTestTags.WATCHLIST_SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(CryptoTestTags.WATCHLIST_SUMMARY).assertIsDisplayed()
        composeRule.onNodeWithTag(CryptoTestTags.WATCHLIST_SEARCH).assertIsDisplayed()
    }

    @Test
    fun watchlistShowsErrorState() {
        val viewModel = initializedViewModel(
            preferences = FakePreferencesRepository(selectedCryptos = setOf("bitcoin")),
            market = FakeMarketRepository(
                priceResult = MarketDataResult.Failure(MarketDataError.Network("offline"))
            )
        )

        composeRule.setContent {
            CryptoAppTheme(darkTheme = false) {
                HomeScreen(viewModel = viewModel)
            }
        }

        composeRule.waitUntilExists(CryptoTestTags.WATCHLIST_ERROR)
        composeRule.onNodeWithTag(CryptoTestTags.WATCHLIST_ERROR).assertIsDisplayed()
    }

    @Test
    fun favoritesShowsEmptyState() {
        val viewModel = initializedViewModel(
            preferences = FakePreferencesRepository(selectedCryptos = setOf("bitcoin")),
            market = FakeMarketRepository()
        )

        composeRule.setContent {
            CryptoAppTheme(darkTheme = false) {
                FavoritesScreen(viewModel = viewModel)
            }
        }

        composeRule.waitUntilExists(CryptoTestTags.FAVORITES_EMPTY)
        composeRule.onNodeWithTag(CryptoTestTags.FAVORITES_SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(CryptoTestTags.FAVORITES_SUMMARY).assertIsDisplayed()
        composeRule.onNodeWithTag(CryptoTestTags.FAVORITES_EMPTY).assertIsDisplayed()
    }

    @Test
    fun portfolioShowsEmptyState() {
        val viewModel = initializedViewModel(
            preferences = FakePreferencesRepository(selectedCryptos = setOf("bitcoin")),
            market = FakeMarketRepository()
        )

        composeRule.setContent {
            CryptoAppTheme(darkTheme = false) {
                PortfolioScreen(viewModel = viewModel)
            }
        }

        composeRule.waitUntilExists(CryptoTestTags.PORTFOLIO_EMPTY)
        composeRule.onNodeWithTag(CryptoTestTags.PORTFOLIO_SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(CryptoTestTags.PORTFOLIO_SUMMARY).assertIsDisplayed()
        composeRule.onNodeWithTag(CryptoTestTags.PORTFOLIO_EMPTY).assertIsDisplayed()
    }

    @Test
    fun portfolioShowsRemoveConfirmation() {
        val viewModel = initializedViewModel(
            preferences = FakePreferencesRepository(
                selectedCryptos = setOf("bitcoin"),
                userCryptos = listOf(
                    UserCrypto(
                        cryptoId = "bitcoin",
                        amount = 1.0,
                        purchasePrice = 100.0,
                        purchaseDateTime = TEST_DATE,
                        transactions = listOf(
                            Transaction(
                                type = TransactionType.BUY,
                                amount = 1.0,
                                price = 100.0,
                                dateTime = TEST_DATE
                            )
                        )
                    )
                )
            ),
            market = FakeMarketRepository()
        )

        composeRule.setContent {
            CryptoAppTheme(darkTheme = false) {
                PortfolioScreen(viewModel = viewModel)
            }
        }

        composeRule.waitUntilExists(CryptoTestTags.PORTFOLIO_LIST)
        composeRule.onNodeWithContentDescription("Remove from portfolio").performClick()

        composeRule.onNodeWithTag(CryptoTestTags.PORTFOLIO_REMOVE_DIALOG).assertIsDisplayed()
        composeRule.onNodeWithTag(CryptoTestTags.PORTFOLIO_REMOVE_CANCEL).assertIsDisplayed()
        composeRule.onNodeWithTag(CryptoTestTags.PORTFOLIO_REMOVE_CONFIRM).assertIsDisplayed()
    }

    @Test
    fun settingsShowsWatchlistSourceSummary() {
        val viewModel = initializedViewModel(
            preferences = FakePreferencesRepository(selectedCryptos = setOf("bitcoin")),
            market = FakeMarketRepository()
        )

        composeRule.setContent {
            CryptoAppTheme(darkTheme = false) {
                SettingsScreen(viewModel = viewModel)
            }
        }

        composeRule.waitUntilExists(CryptoTestTags.SETTINGS_SUMMARY)
        composeRule.onNodeWithTag(CryptoTestTags.SETTINGS_SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(CryptoTestTags.SETTINGS_SUMMARY).assertIsDisplayed()
    }

    private fun initializedViewModel(
        preferences: FakePreferencesRepository,
        market: FakeMarketRepository
    ): CryptoViewModel {
        val viewModel = CryptoViewModel(preferences, market)
        runBlocking {
            viewModel.initialize()
        }
        composeRule.waitForIdle()
        return viewModel
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.waitUntilExists(tag: String) {
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        val TEST_DATE: LocalDateTime = LocalDateTime.of(2026, 6, 4, 12, 0)
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
    private val priceResult: MarketDataResult<Map<String, Double>> = MarketDataResult.Success(
        mapOf(
            "bitcoin" to 60_000.0,
            "ethereum" to 3_000.0
        )
    )
) : MarketRepository {
    override suspend fun getAllAvailableCryptos(): List<CryptoAsset> {
        return listOf(
            CryptoAsset("bitcoin", "btc", "Bitcoin", 1),
            CryptoAsset("ethereum", "eth", "Ethereum", 2)
        )
    }

    override suspend fun getCryptoPrices(
        coinIds: List<String>,
        currency: String
    ): Map<String, Double> {
        return when (val result = getCryptoPricesResult(coinIds, currency)) {
            is MarketDataResult.Success -> result.value
            is MarketDataResult.PartialSuccess -> result.value
            is MarketDataResult.Failure -> emptyMap()
        }
    }

    override suspend fun getCryptoPricesResult(
        coinIds: List<String>,
        currency: String
    ): MarketDataResult<Map<String, Double>> = priceResult
}

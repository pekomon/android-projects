package com.pekomon.cryptoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.pekomon.cryptoapp.core.formatting.DisplayFormatters
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.ui.AssetMetadataSource
import com.pekomon.cryptoapp.ui.CryptoViewModel
import com.pekomon.cryptoapp.ui.MarketLoadState
import com.pekomon.cryptoapp.ui.components.CommonCard
import com.pekomon.cryptoapp.ui.components.CryptoListItemRow
import com.pekomon.cryptoapp.ui.components.MarketStatusCard
import com.pekomon.cryptoapp.ui.components.QuickAddDialog
import com.pekomon.cryptoapp.ui.components.ScreenHeader
import com.pekomon.cryptoapp.ui.components.SortMenu
import com.pekomon.cryptoapp.ui.components.StateMessageCard
import com.pekomon.cryptoapp.ui.testing.CryptoTestTags
import com.pekomon.cryptoapp.ui.theme.CryptoSpacing
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun HomeScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    var quickAddCrypto by remember { mutableStateOf<CryptoAsset?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val state = viewModel.watchlistUiState
    val totalWatchlistAssets = state.assets.size
    val pricedWatchlistAssets = state.assets.count { crypto ->
        state.prices[crypto.id] != null
    }
    val missingPriceAssets = (totalWatchlistAssets - pricedWatchlistAssets).coerceAtLeast(0)
    val visibleCryptos = remember(state.assets, searchQuery) {
        state.assets.filter { crypto ->
            searchQuery.isBlank() ||
                crypto.name.contains(searchQuery, ignoreCase = true) ||
                crypto.symbol.contains(searchQuery, ignoreCase = true)
        }
    }
    
    SwipeRefresh(
        state = rememberSwipeRefreshState(state.isLoading),
        onRefresh = { viewModel.fetchPrices() },
        modifier = modifier
            .fillMaxSize()
            .testTag(CryptoTestTags.WATCHLIST_SCREEN)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(CryptoSpacing.large)
                .testTag(CryptoTestTags.WATCHLIST_LIST),
            verticalArrangement = Arrangement.spacedBy(CryptoSpacing.large)
        ) {
            item {
                ScreenHeader(title = "Watchlist")
            }

            item {
                MarketStatusCard(
                    title = "Sort by: ${state.sortOption.displayName}",
                    marketLoadState = state.marketLoadState,
                    assetMetadataSource = state.assetMetadataSource
                        ?.takeUnless { it == AssetMetadataSource.Live },
                    isLoading = state.isLoading
                ) {
                    SortMenu(
                        currentSort = state.sortOption,
                        onSortSelected = { viewModel.updateSortOption(it) }
                    )
                }
            }

            item {
                WatchlistSummaryCard(
                    totalAssets = totalWatchlistAssets,
                    pricedAssets = pricedWatchlistAssets,
                    missingPriceAssets = missingPriceAssets,
                    modifier = Modifier.testTag(CryptoTestTags.WATCHLIST_SUMMARY)
                )
            }

            item {
                CommonCard(
                    modifier = Modifier.testTag(CryptoTestTags.WATCHLIST_SEARCH)
                ) {
                    Column(
                        modifier = Modifier.padding(CryptoSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(CryptoSpacing.small)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search watchlist") },
                            placeholder = { Text("Bitcoin, ETH, SOL...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Text(
                            text = searchResultLabel(
                                visibleCount = visibleCryptos.size,
                                totalCount = totalWatchlistAssets,
                                searchQuery = searchQuery
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            when {
                state.isLoading && state.assets.isEmpty() -> {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(CryptoTestTags.WATCHLIST_LOADING)
                        )
                    }
                }
                state.marketLoadState is MarketLoadState.Error -> {
                    item {
                        val loadState = state.marketLoadState as MarketLoadState.Error
                        StateMessageCard(
                            title = "Prices unavailable",
                            message = loadState.message,
                            actionLabel = "Retry",
                            onAction = { viewModel.fetchPrices() },
                            modifier = Modifier.testTag(CryptoTestTags.WATCHLIST_ERROR)
                        )
                    }
                }
                state.assets.isEmpty() -> {
                    item {
                        StateMessageCard(
                            title = "No assets selected",
                            message = "Open Settings and choose the cryptocurrencies you want on your watchlist.",
                            modifier = Modifier.testTag(CryptoTestTags.WATCHLIST_EMPTY)
                        )
                    }
                }
                visibleCryptos.isEmpty() -> {
                    item {
                        StateMessageCard(
                            title = "No matches",
                            message = "Try another asset name or symbol.",
                            modifier = Modifier.testTag(CryptoTestTags.WATCHLIST_NO_MATCHES)
                        )
                    }
                }
                else -> {
                    items(visibleCryptos) { crypto ->
                        val cryptoInfo = viewModel.getCryptoInfo(crypto.id)
                        CryptoListItemRow(
                            crypto = crypto,
                            currentPrice = cryptoInfo?.currentPrice,
                            priceChangePercentage = cryptoInfo?.priceChangePercentage ?: 0.0,
                            currency = viewModel.selectedCurrency,
                            isFavorite = viewModel.isFavorite(crypto.id),
                            onFavoriteClick = { viewModel.toggleFavorite(crypto.id) },
                            onQuickAdd = { quickAddCrypto = crypto }
                        )
                    }
                }
            }
        }
    }
    
    quickAddCrypto?.let { crypto ->
        val currentPrice = state.prices[crypto.id]?.currentPrice
        QuickAddDialog(
            cryptoName = crypto.name,
            currentPrice = currentPrice,
            currency = state.selectedCurrency,
            onDismiss = { quickAddCrypto = null },
            onConfirm = { amount, price, dateTime ->
                viewModel.addUserCrypto(crypto.id, amount, price, dateTime)
                quickAddCrypto = null
            }
        )
    }
}

@Composable
private fun WatchlistSummaryCard(
    totalAssets: Int,
    pricedAssets: Int,
    missingPriceAssets: Int,
    modifier: Modifier = Modifier
) {
    CommonCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(CryptoSpacing.large)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CryptoSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryMetric(
                label = "Watchlist",
                value = totalAssets.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                label = "Live prices",
                value = pricedAssets.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                label = "Unavailable",
                value = missingPriceAssets.toString(),
                modifier = Modifier.weight(1f),
                emphasized = missingPriceAssets > 0
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(CryptoSpacing.xSmall)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = if (emphasized) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun searchResultLabel(
    visibleCount: Int,
    totalCount: Int,
    searchQuery: String
): String = when {
    totalCount == 0 -> "Add assets in Settings to build your watchlist."
    searchQuery.isBlank() -> "Showing all $totalCount watchlist assets."
    else -> "Showing $visibleCount of $totalCount assets for \"$searchQuery\"."
}

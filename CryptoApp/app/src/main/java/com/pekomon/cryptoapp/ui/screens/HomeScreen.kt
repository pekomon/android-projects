package com.pekomon.cryptoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.pekomon.cryptoapp.core.formatting.DisplayFormatters
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.ui.AssetMetadataSource
import com.pekomon.cryptoapp.ui.CryptoViewModel
import com.pekomon.cryptoapp.ui.MarketLoadState
import com.pekomon.cryptoapp.ui.components.CommonCard
import com.pekomon.cryptoapp.ui.components.CryptoList
import com.pekomon.cryptoapp.ui.components.QuickAddDialog
import com.pekomon.cryptoapp.ui.components.ScreenHeader
import com.pekomon.cryptoapp.ui.components.SortMenu
import com.pekomon.cryptoapp.ui.components.StateMessageCard
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun HomeScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    var quickAddCrypto by remember { mutableStateOf<CryptoAsset?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val totalWatchlistAssets = viewModel.sortedCryptos.size
    val pricedWatchlistAssets = viewModel.sortedCryptos.count { crypto ->
        viewModel.getCryptoInfo(crypto.id) != null
    }
    val missingPriceAssets = (totalWatchlistAssets - pricedWatchlistAssets).coerceAtLeast(0)
    val visibleCryptos = remember(viewModel.sortedCryptos, searchQuery) {
        viewModel.sortedCryptos.filter { crypto ->
            searchQuery.isBlank() ||
                crypto.name.contains(searchQuery, ignoreCase = true) ||
                crypto.symbol.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "Watchlist")
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sort by: ${viewModel.currentSortOption.displayName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                (viewModel.marketLoadState as? MarketLoadState.Content)?.let { state ->
                    Text(
                        text = DisplayFormatters.updateTime(state.lastUpdated),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                viewModel.assetMetadataSource
                    ?.takeUnless { it == AssetMetadataSource.Live }
                    ?.let { source ->
                        Text(
                            text = source.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
            }
            SortMenu(
                currentSort = viewModel.currentSortOption,
                onSortSelected = { viewModel.updateSortOption(it) }
            )
        }

        WatchlistSummaryCard(
            totalAssets = totalWatchlistAssets,
            pricedAssets = pricedWatchlistAssets,
            missingPriceAssets = missingPriceAssets
        )

        CommonCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search watchlist") },
                    placeholder = { Text("Bitcoin, ETH, SOL...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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

        when {
            viewModel.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            viewModel.error != null -> {
                StateMessageCard(
                    title = "Prices unavailable",
                    message = viewModel.error ?: "Unable to load prices.",
                    actionLabel = "Retry",
                    onAction = { viewModel.fetchPrices() }
                )
            }
            viewModel.sortedCryptos.isEmpty() -> {
                StateMessageCard(
                    title = "No assets selected",
                    message = "Open Settings and choose the cryptocurrencies you want on your watchlist."
                )
            }
            visibleCryptos.isEmpty() -> {
                StateMessageCard(
                    title = "No matches",
                    message = "Try another asset name or symbol."
                )
            }
            else -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(viewModel.isLoading),
                    onRefresh = { viewModel.fetchPrices() },
                    modifier = Modifier.weight(1f)
                ) {
                    CryptoList(
                        cryptos = visibleCryptos,
                        viewModel = viewModel,
                        onQuickAdd = { crypto -> quickAddCrypto = crypto }
                    )
                }
            }
        }
    }
    
    quickAddCrypto?.let { crypto ->
        val currentPrice = viewModel.getCryptoInfo(crypto.id)?.currentPrice ?: 0.0
        QuickAddDialog(
            cryptoName = crypto.name,
            currentPrice = currentPrice,
            currency = viewModel.selectedCurrency,
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
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
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
        verticalArrangement = Arrangement.spacedBy(2.dp)
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

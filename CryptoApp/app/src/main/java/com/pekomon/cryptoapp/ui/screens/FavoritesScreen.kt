package com.pekomon.cryptoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pekomon.cryptoapp.core.formatting.DisplayFormatters
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.ui.MarketLoadState
import com.pekomon.cryptoapp.ui.CryptoViewModel
import com.pekomon.cryptoapp.ui.components.CommonCard
import com.pekomon.cryptoapp.ui.components.CryptoList
import com.pekomon.cryptoapp.ui.components.QuickAddDialog
import com.pekomon.cryptoapp.ui.components.ScreenHeader
import com.pekomon.cryptoapp.ui.components.SortMenu
import com.pekomon.cryptoapp.ui.components.StateMessageCard
import com.pekomon.cryptoapp.ui.theme.CryptoSpacing
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun FavoritesScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    var quickAddCrypto by remember { mutableStateOf<CryptoAsset?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.padding(CryptoSpacing.large),
        verticalArrangement = Arrangement.spacedBy(CryptoSpacing.large)
    ) {
        ScreenHeader(title = "Favorites")
        val favorites = viewModel.sortedFavoriteCryptos
        val pricedFavorites = favorites.count { crypto -> viewModel.getCryptoInfo(crypto.id) != null }
        
        OutlinedButton(
            onClick = { showSortMenu = true },
            modifier = Modifier.fillMaxWidth()
        ) {
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
                        if (state.isStale || state.message != null) {
                            Text(
                                text = state.message ?: "Using last successful prices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort"
                )
            }
        }
        
        if (showSortMenu) {
            SortMenu(
                currentSort = viewModel.currentSortOption,
                onSortSelected = { 
                    viewModel.updateSortOption(it)
                    showSortMenu = false
                }
            )
        }

        FavoritesSummaryCard(
            favoriteCount = favorites.size,
            pricedCount = pricedFavorites
        )

        when {
            viewModel.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            viewModel.marketLoadState is MarketLoadState.Error -> {
                val state = viewModel.marketLoadState as MarketLoadState.Error
                StateMessageCard(
                    title = "Favorites unavailable",
                    message = state.message,
                    actionLabel = "Retry",
                    onAction = { viewModel.fetchPrices() }
                )
            }
            else -> {
                if (favorites.isEmpty()) {
                    StateMessageCard(
                        title = "No favorites yet",
                        message = "Tap the heart icon on any asset to keep it here for quick monitoring."
                    )
                } else {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(viewModel.isLoading),
                        onRefresh = { viewModel.fetchPrices() },
                        modifier = Modifier.weight(1f)
                    ) {
                        CryptoList(
                            cryptos = favorites,
                            viewModel = viewModel,
                            onQuickAdd = { crypto -> 
                                quickAddCrypto = crypto
                            }
                        )
                    }
                }
            }
        }
    }
    
    quickAddCrypto?.let { crypto ->
        val currentPrice = viewModel.getCryptoInfo(crypto.id)?.currentPrice
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
private fun FavoritesSummaryCard(
    favoriteCount: Int,
    pricedCount: Int,
    modifier: Modifier = Modifier
) {
    CommonCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CryptoSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(CryptoSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FavoriteMetric(
                label = "Favorites",
                value = favoriteCount.toString(),
                modifier = Modifier.weight(1f)
            )
            FavoriteMetric(
                label = "Priced",
                value = pricedCount.toString(),
                modifier = Modifier.weight(1f)
            )
            FavoriteMetric(
                label = "Watch",
                value = (favoriteCount - pricedCount).coerceAtLeast(0).toString(),
                modifier = Modifier.weight(1f),
                emphasized = favoriteCount > pricedCount
            )
        }
    }
}

@Composable
private fun FavoriteMetric(
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
            style = MaterialTheme.typography.titleLarge,
            color = if (emphasized) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

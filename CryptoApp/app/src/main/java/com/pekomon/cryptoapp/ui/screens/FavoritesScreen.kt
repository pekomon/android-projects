package com.pekomon.cryptoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.ui.MarketLoadState
import com.pekomon.cryptoapp.ui.CryptoViewModel
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
fun FavoritesScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    var quickAddCrypto by remember { mutableStateOf<CryptoAsset?>(null) }
    val state = viewModel.favoritesUiState
    
    val favorites = state.assets
    val pricedFavorites = favorites.count { crypto -> state.prices[crypto.id] != null }

    SwipeRefresh(
        state = rememberSwipeRefreshState(state.isLoading),
        onRefresh = { viewModel.fetchPrices() },
        modifier = modifier
            .fillMaxSize()
            .testTag(CryptoTestTags.FAVORITES_SCREEN)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(CryptoSpacing.large)
                .testTag(CryptoTestTags.FAVORITES_LIST),
            verticalArrangement = Arrangement.spacedBy(CryptoSpacing.large)
        ) {
            item {
                ScreenHeader(title = "Favorites")
            }

            item {
                MarketStatusCard(
                    title = "Sort by: ${state.sortOption.displayName}",
                    marketLoadState = state.marketLoadState,
                    isLoading = state.isLoading
                ) {
                    SortMenu(
                        currentSort = state.sortOption,
                        onSortSelected = { viewModel.updateSortOption(it) }
                    )
                }
            }

            item {
                FavoritesSummaryCard(
                    favoriteCount = favorites.size,
                    pricedCount = pricedFavorites,
                    modifier = Modifier.testTag(CryptoTestTags.FAVORITES_SUMMARY)
                )
            }

            when {
                state.marketLoadState is MarketLoadState.Error -> {
                    item {
                        val loadState = state.marketLoadState as MarketLoadState.Error
                        StateMessageCard(
                            title = "Favorites unavailable",
                            message = loadState.message,
                            actionLabel = "Retry",
                            onAction = { viewModel.fetchPrices() },
                            modifier = Modifier.testTag(CryptoTestTags.FAVORITES_ERROR)
                        )
                    }
                }
                favorites.isEmpty() -> {
                    item {
                        StateMessageCard(
                            title = "No favorites yet",
                            message = "Tap the heart icon on any asset to keep it here for quick monitoring.",
                            modifier = Modifier.testTag(CryptoTestTags.FAVORITES_EMPTY)
                        )
                    }
                }
                else -> {
                    items(favorites) { crypto ->
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

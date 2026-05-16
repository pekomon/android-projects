package com.pekomon.cryptoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pekomon.cryptoapp.data.CryptoListItem
import com.pekomon.cryptoapp.ui.CryptoViewModel
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
    var quickAddCrypto by remember { mutableStateOf<CryptoListItem?>(null) }
    
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
            Text(
                text = "Sort by: ${viewModel.currentSortOption.displayName}",
                style = MaterialTheme.typography.bodyLarge
            )
            SortMenu(
                currentSort = viewModel.currentSortOption,
                onSortSelected = { viewModel.updateSortOption(it) }
            )
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
                    message = viewModel.error ?: "Unable to load prices."
                )
            }
            viewModel.sortedCryptos.isEmpty() -> {
                StateMessageCard(
                    title = "No assets selected",
                    message = "Open Settings and choose the cryptocurrencies you want on your watchlist."
                )
            }
            else -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(viewModel.isLoading),
                    onRefresh = { viewModel.fetchPrices() },
                    modifier = Modifier.weight(1f)
                ) {
                    CryptoList(
                        cryptos = viewModel.sortedCryptos,
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

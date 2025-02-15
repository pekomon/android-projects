package com.example.pekomon.cryptoapp.ui.screens

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
import androidx.compose.ui.unit.dp
import com.example.pekomon.cryptoapp.data.CryptoListItem
import com.example.pekomon.cryptoapp.ui.CryptoViewModel
import com.example.pekomon.cryptoapp.ui.components.CryptoList
import com.example.pekomon.cryptoapp.ui.components.QuickAddDialog
import com.example.pekomon.cryptoapp.ui.components.SortMenu
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun FavoritesScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    var quickAddCrypto by remember { mutableStateOf<CryptoListItem?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Favorite Cryptocurrencies",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedButton(
            onClick = { showSortMenu = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort by: ${viewModel.currentSortOption.displayName}",
                    style = MaterialTheme.typography.bodyLarge
                )
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

        when {
            viewModel.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            viewModel.error != null -> {
                Text(
                    text = viewModel.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                val favorites = viewModel.sortedCryptos.filter { crypto -> 
                    viewModel.isFavorite(crypto.id)
                }
                
                if (favorites.isEmpty()) {
                    Text(
                        text = "No favorites yet",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
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
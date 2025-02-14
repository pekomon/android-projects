import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.pekomon.cryptoapp.data.CryptoListItem
import com.example.pekomon.cryptoapp.ui.CryptoViewModel
import com.example.pekomon.cryptoapp.ui.components.CryptoListItemRow
import com.example.pekomon.cryptoapp.ui.components.QuickAddDialog
import com.example.pekomon.cryptoapp.ui.components.SortMenu
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
        Text(
            text = "Cryptocurrencies",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )
        
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
                Text(
                    text = viewModel.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(viewModel.isLoading),
                    onRefresh = { viewModel.fetchPrices() },
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.sortedCryptos) { crypto ->
                            val cryptoInfo = viewModel.getCryptoInfo(crypto.id)
                            CryptoListItemRow(
                                crypto = crypto,
                                currentPrice = cryptoInfo?.currentPrice ?: 0.0,
                                priceChangePercentage = cryptoInfo?.priceChangePercentage ?: 0.0,
                                currency = viewModel.selectedCurrency,
                                isFavorite = viewModel.isFavorite(crypto.id),
                                onFavoriteClick = { 
                                    Log.d("HomeScreen", "Toggling favorite for ${crypto.name}")
                                    viewModel.toggleFavorite(crypto.id) 
                                },
                                onQuickAdd = {
                                    Log.d("HomeScreen", "Quick adding ${crypto.name}")
                                    quickAddCrypto = crypto
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    quickAddCrypto?.let { crypto ->
        Log.d("HomeScreen", "Showing QuickAddDialog for ${crypto.name}")
        QuickAddDialog(
            cryptoName = crypto.name,
            onDismiss = { 
                Log.d("HomeScreen", "Dismissing QuickAddDialog for ${crypto.name}")
                quickAddCrypto = null 
            },
            onConfirm = { amount ->
                Log.d("HomeScreen", "Adding ${crypto.name} with amount $amount")
                viewModel.addUserCrypto(crypto.id, amount)
                quickAddCrypto = null
            }
        )
    }
} 
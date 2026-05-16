package com.pekomon.cryptoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.pekomon.cryptoapp.data.CryptoListItem
import com.pekomon.cryptoapp.ui.CryptoViewModel

@Composable
fun CryptoList(
    cryptos: List<CryptoListItem>,
    viewModel: CryptoViewModel,
    onQuickAdd: (CryptoListItem) -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(cryptos) { crypto ->
            val cryptoInfo = viewModel.getCryptoInfo(crypto.id)
            CryptoListItemRow(
                crypto = crypto,
                currentPrice = cryptoInfo?.currentPrice ?: 0.0,
                priceChangePercentage = cryptoInfo?.priceChangePercentage ?: 0.0,
                currency = viewModel.selectedCurrency,
                isFavorite = viewModel.isFavorite(crypto.id),
                onFavoriteClick = { viewModel.toggleFavorite(crypto.id) },
                onQuickAdd = { onQuickAdd(crypto) }
            )
        }
    }
} 
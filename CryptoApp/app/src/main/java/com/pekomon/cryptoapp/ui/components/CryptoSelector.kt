package com.pekomon.cryptoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.ui.theme.CryptoSpacing

@Composable
fun CryptoSelector(
    availableCryptos: List<CryptoAsset>,
    selectedCryptos: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(CryptoSpacing.small)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search watchlist assets") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "${selectedCryptos.size} selected",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(CryptoSpacing.xSmall)
        ) {
            val filteredCryptos = availableCryptos.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.symbol.contains(searchQuery, ignoreCase = true)
            }.sortedBy { it.marketCapRank ?: Int.MAX_VALUE }

            if (filteredCryptos.isEmpty()) {
                item {
                    Text(
                        text = "No assets match your search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(CryptoSpacing.small)
                    )
                }
            }

            items(filteredCryptos) { crypto ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = crypto.id in selectedCryptos,
                            onClick = {
                                val newSelection = if (crypto.id in selectedCryptos) {
                                    selectedCryptos - crypto.id
                                } else {
                                    selectedCryptos + crypto.id
                                }
                                onSelectionChanged(newSelection)
                            }
                        )
                        .padding(vertical = CryptoSpacing.small),
                    horizontalArrangement = Arrangement.spacedBy(CryptoSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(CryptoSpacing.xSmall)
                    ) {
                        Text(
                            text = crypto.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = crypto.symbol.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Checkbox(
                        checked = crypto.id in selectedCryptos,
                        onCheckedChange = null
                    )
                }
            }
        }
    }
}

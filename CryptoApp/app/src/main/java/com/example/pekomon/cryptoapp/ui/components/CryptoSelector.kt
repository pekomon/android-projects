package com.example.pekomon.cryptoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pekomon.cryptoapp.data.CryptoListItem

@Composable
fun CryptoSelector(
    availableCryptos: List<CryptoListItem>,
    selectedCryptos: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        modifier = Modifier.padding(8.dp)
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
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = crypto.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = crypto.symbol.uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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

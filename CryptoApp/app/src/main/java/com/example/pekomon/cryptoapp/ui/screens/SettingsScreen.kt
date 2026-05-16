package com.example.pekomon.cryptoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pekomon.cryptoapp.data.Currency
import com.example.pekomon.cryptoapp.ui.CryptoViewModel
import com.example.pekomon.cryptoapp.ui.components.CryptoSelector
import com.example.pekomon.cryptoapp.ui.components.ScreenHeader
import com.example.pekomon.cryptoapp.ui.components.SortMenu

@Composable
fun SettingsScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    var showCryptoSelector by remember { mutableStateOf(false) }

    if (showCryptoSelector) {
        AlertDialog(
            onDismissRequest = { showCryptoSelector = false },
            title = { Text("Select cryptocurrencies") },
            text = {
                CryptoSelector(
                    availableCryptos = viewModel.availableCryptos,
                    selectedCryptos = viewModel.selectedCryptos,
                    onSelectionChanged = { viewModel.updateSelectedCryptos(it) }
                )
            },
            confirmButton = {
                TextButton(onClick = { showCryptoSelector = false }) {
                    Text("Done")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "Settings")

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Currency",
                    style = MaterialTheme.typography.titleMedium
                )

                Currency.entries.forEach { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currency == viewModel.selectedCurrency,
                                onClick = { viewModel.updateCurrency(currency) }
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currency == viewModel.selectedCurrency,
                            onClick = { viewModel.updateCurrency(currency) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${currency.name} (${currency.symbol})",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Sorting",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = viewModel.currentSortOption.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Used across Watchlist and Favorites",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    SortMenu(
                        currentSort = viewModel.currentSortOption,
                        onSortSelected = { viewModel.updateSortOption(it) }
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Watchlist",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${viewModel.selectedCryptos.size} assets selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Watchlist assets appear on Home. Favorites are marked separately with the heart action.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = { showCryptoSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit watchlist")
                }
            }
        }
    }
}

package com.pekomon.cryptoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.testTag
import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.ui.CryptoViewModel
import com.pekomon.cryptoapp.ui.components.CommonCard
import com.pekomon.cryptoapp.ui.components.CryptoSelector
import com.pekomon.cryptoapp.ui.components.MarketStatusCard
import com.pekomon.cryptoapp.ui.components.ScreenHeader
import com.pekomon.cryptoapp.ui.components.SortMenu
import com.pekomon.cryptoapp.ui.testing.CryptoTestTags
import com.pekomon.cryptoapp.ui.theme.CryptoSpacing

@Composable
fun SettingsScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    var showCryptoSelector by remember { mutableStateOf(false) }
    val state = viewModel.settingsUiState

    if (showCryptoSelector) {
        AlertDialog(
            onDismissRequest = { showCryptoSelector = false },
            title = { Text("Select cryptocurrencies") },
            text = {
                CryptoSelector(
                    availableCryptos = state.availableAssets,
                    selectedCryptos = state.selectedAssetIds,
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(CryptoTestTags.SETTINGS_SCREEN)
            .padding(CryptoSpacing.large),
        verticalArrangement = Arrangement.spacedBy(CryptoSpacing.large)
    ) {
        item {
            ScreenHeader(title = "Settings")
        }

        item {
            MarketStatusCard(
                title = "Watchlist source",
                subtitle = "${state.selectedAssetIds.size} assets selected • ${state.favoriteIds.size} favorites",
                assetMetadataSource = state.assetMetadataSource,
                isLoading = state.isLoading,
                modifier = Modifier.testTag(CryptoTestTags.SETTINGS_SUMMARY)
            )
        }

        item {
            SettingsSection(
                title = "Currency",
                subtitle = "Market values, portfolio totals, and transaction prices use this display currency."
            ) {
                Currency.entries.forEach { currency ->
                    CurrencyOption(
                        currency = currency,
                        selected = currency == state.selectedCurrency,
                        onClick = { viewModel.updateCurrency(currency) }
                    )
                }
            }
        }

        item {
            SettingsSection(
                title = "Sorting",
                subtitle = "Used across Watchlist and Favorites."
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CryptoSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(CryptoSpacing.xSmall)
                    ) {
                        Text(
                            text = state.sortOption.displayName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Current order",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    SortMenu(
                        currentSort = state.sortOption,
                        onSortSelected = { viewModel.updateSortOption(it) }
                    )
                }
            }
        }

        item {
            SettingsSection(
                title = "Watchlist",
                subtitle = "Home shows these assets. Favorites are a separate quick-monitoring subset."
            ) {
                Text(
                    text = "${state.selectedAssetIds.size} assets selected",
                    style = MaterialTheme.typography.titleMedium
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

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    CommonCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CryptoSpacing.large),
            verticalArrangement = Arrangement.spacedBy(CryptoSpacing.medium)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(CryptoSpacing.xSmall)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

@Composable
private fun CurrencyOption(
    currency: Currency,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(vertical = CryptoSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CryptoSpacing.small)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Column(verticalArrangement = Arrangement.spacedBy(CryptoSpacing.xSmall)) {
            Text(
                text = "${currency.name} (${currency.symbol})",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (selected) "Selected" else "Available",
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

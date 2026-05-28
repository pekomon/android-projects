package com.pekomon.cryptoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.pekomon.cryptoapp.core.formatting.DisplayFormatters
import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.domain.portfolio.PortfolioCalculator
import com.pekomon.cryptoapp.domain.portfolio.PortfolioHoldingMetrics
import com.pekomon.cryptoapp.domain.portfolio.PortfolioSummaryMetrics
import com.pekomon.cryptoapp.ui.CryptoViewModel
import com.pekomon.cryptoapp.ui.components.CommonCard
import com.pekomon.cryptoapp.ui.components.QuickAddDialog
import com.pekomon.cryptoapp.ui.components.ScreenHeader
import com.pekomon.cryptoapp.ui.components.StateMessageCard
import com.pekomon.cryptoapp.ui.components.TransactionDialog

@Composable
fun PortfolioScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    var editingCrypto by remember { mutableStateOf<UserCrypto?>(null) }
    var showTransactionDialog by remember { mutableStateOf<UserCrypto?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "Portfolio")

        val holdings = viewModel.getCombinedUserCryptos()
        val portfolioMetrics = remember(holdings, viewModel.marketLoadState) {
            PortfolioCalculator.summaryMetrics(holdings) { cryptoId ->
                viewModel.getCryptoInfo(cryptoId)?.currentPrice
            }
        }
        val holdingMetrics = remember(holdings, viewModel.marketLoadState) {
            PortfolioCalculator.holdingMetrics(holdings) { cryptoId ->
                viewModel.getCryptoInfo(cryptoId)?.currentPrice
            }.associateBy { it.cryptoId }
        }

        PortfolioSummaryCard(
            metrics = portfolioMetrics,
            currency = viewModel.selectedCurrency
        )

        if (holdings.isEmpty()) {
            StateMessageCard(
                title = "No holdings yet",
                message = "Add an asset from Watchlist to start tracking value, cost basis, and profit/loss."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(holdings) { userCrypto ->
                    val crypto = viewModel.availableCryptos.find { it.id == userCrypto.cryptoId }

                    if (crypto != null) {
                        PortfolioItem(
                            cryptoName = crypto.name,
                            cryptoSymbol = crypto.symbol.uppercase(),
                            userCrypto = userCrypto,
                            metrics = holdingMetrics[userCrypto.cryptoId],
                            currency = viewModel.selectedCurrency,
                            onEdit = { editingCrypto = userCrypto },
                            onDelete = { viewModel.removeUserCrypto(userCrypto.cryptoId) },
                            onClick = { showTransactionDialog = userCrypto }
                        )
                    }
                }
            }
        }
    }
    
    editingCrypto?.let { crypto ->
        val cryptoName = viewModel.availableCryptos.find { it.id == crypto.cryptoId }?.name ?: "Unknown"
        val currentPrice = viewModel.getCryptoInfo(crypto.cryptoId)?.currentPrice
        QuickAddDialog(
            cryptoName = cryptoName,
            currentPrice = currentPrice,
            currency = viewModel.selectedCurrency,
            onDismiss = { editingCrypto = null },
            onConfirm = { amount, price, dateTime ->
                viewModel.updateUserCrypto(crypto.cryptoId, amount, price, dateTime)
                editingCrypto = null
            },
            initialAmount = crypto.amount,
            initialPrice = currentPrice,
            title = "Update $cryptoName",
            confirmLabel = "Update"
        )
    }
    
    showTransactionDialog?.let { crypto ->
        val cryptoName = viewModel.availableCryptos.find { it.id == crypto.cryptoId }?.name ?: "Unknown"
        val currentPrice = viewModel.getCryptoInfo(crypto.cryptoId)?.currentPrice ?: 0.0
        
        TransactionDialog(
            cryptoName = cryptoName,
            transactions = crypto.transactions,
            currentPrice = currentPrice,
            initialPrice = crypto.purchasePrice,
            currency = viewModel.selectedCurrency,
            onDismiss = { showTransactionDialog = null }
        )
    }
}

@Composable
private fun PortfolioItem(
    cryptoName: String,
    cryptoSymbol: String,
    userCrypto: UserCrypto,
    metrics: PortfolioHoldingMetrics?,
    currency: Currency,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val amount = metrics?.amount ?: userCrypto.amount
    val costBasis = metrics?.costBasis ?: userCrypto.purchasePrice * userCrypto.amount

    CommonCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cryptoName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$cryptoSymbol • $amount held",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    val currentValue = metrics?.currentValue
                    val profitLoss = metrics?.profitLoss
                    val profitLossPercentage = metrics?.profitLossPercentage

                    if (currentValue == null || profitLoss == null || profitLossPercentage == null) {
                        Text(
                            text = "Value unavailable",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Waiting for market price",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = DisplayFormatters.currencyAmount(currentValue, currency),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${DisplayFormatters.signedCurrencyAmount(profitLoss, currency)} (${DisplayFormatters.percentage(profitLossPercentage)})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (profitLoss >= 0) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit amount")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Remove from portfolio")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HoldingMetric(
                    label = "Cost basis",
                    value = DisplayFormatters.currencyAmount(costBasis, currency),
                    modifier = Modifier.weight(1f)
                )
                HoldingMetric(
                    label = "Avg buy",
                    value = DisplayFormatters.currencyAmount(metrics?.averageCost ?: userCrypto.purchasePrice, currency),
                    modifier = Modifier.weight(1f)
                )
                HoldingMetric(
                    label = "Current",
                    value = metrics?.currentPrice?.let { DisplayFormatters.currencyAmount(it, currency) } ?: "Unavailable",
                    modifier = Modifier.weight(1f)
                )
            }

            if (metrics != null && metrics.currentValue != null) {
                HoldingMetric(
                    label = "Allocation",
                    value = DisplayFormatters.percentage(metrics.allocationPercentage),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PortfolioSummaryCard(
    metrics: PortfolioSummaryMetrics,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    CommonCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Portfolio Value",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = DisplayFormatters.currencyAmount(metrics.currentValue, currency),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "${metrics.holdingCount} active holdings • ${metrics.pricedHoldingCount} priced",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PortfolioMetric(
                    label = "Invested",
                    value = DisplayFormatters.currencyAmount(metrics.investedValue, currency),
                    modifier = Modifier.weight(1f)
                )
                PortfolioMetric(
                    label = "P/L",
                    value = DisplayFormatters.signedCurrencyAmount(metrics.profitLoss, currency),
                    modifier = Modifier.weight(1f),
                    valueTone = metrics.profitLoss.tone()
                )
                PortfolioMetric(
                    label = "Return",
                    value = DisplayFormatters.percentage(metrics.profitLossPercentage),
                    modifier = Modifier.weight(1f),
                    valueTone = metrics.profitLoss.tone()
                )
            }

            if (metrics.unpricedHoldingCount > 0) {
                Text(
                    text = "${metrics.unpricedHoldingCount} holdings are excluded from live value until prices are available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PortfolioMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueTone: ValueTone = ValueTone.Neutral
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueTone.color()
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HoldingMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private enum class ValueTone {
    Positive,
    Negative,
    Neutral
}

private fun Double.tone(): ValueTone = when {
    this > 0.0 -> ValueTone.Positive
    this < 0.0 -> ValueTone.Negative
    else -> ValueTone.Neutral
}

@Composable
private fun ValueTone.color() = when (this) {
    ValueTone.Positive -> MaterialTheme.colorScheme.tertiary
    ValueTone.Negative -> MaterialTheme.colorScheme.error
    ValueTone.Neutral -> MaterialTheme.colorScheme.onSurface
}

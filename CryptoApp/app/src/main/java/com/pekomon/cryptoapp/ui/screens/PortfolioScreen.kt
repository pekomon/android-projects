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
import androidx.compose.material3.Card
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
import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.UserCrypto
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
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total Value",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${viewModel.selectedCurrency.symbol}%.2f".format(viewModel.totalPortfolioValue),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "${viewModel.getCombinedUserCryptos().size} active holdings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val holdings = viewModel.getCombinedUserCryptos()
        if (holdings.isEmpty()) {
            StateMessageCard(
                title = "No holdings yet",
                message = "Use the add button from Watchlist or Favorites to track an asset in your portfolio."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(holdings) { userCrypto ->
                    val crypto = viewModel.availableCryptos.find { it.id == userCrypto.cryptoId }
                    val cryptoInfo = viewModel.getCryptoInfo(userCrypto.cryptoId)

                    if (crypto != null) {
                        PortfolioItem(
                            cryptoName = crypto.name,
                            cryptoSymbol = crypto.symbol.uppercase(),
                            userCrypto = userCrypto,
                            currentPrice = cryptoInfo?.currentPrice ?: 0.0,
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
        val currentPrice = viewModel.getCryptoInfo(crypto.cryptoId)?.currentPrice ?: 0.0
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
    currentPrice: Double,
    currency: Currency,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    CommonCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
                        text = "$cryptoSymbol • ${userCrypto.amount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    val totalValue = currentPrice * userCrypto.amount
                    val initialValue = userCrypto.purchasePrice * userCrypto.amount
                    val valueChange = totalValue - initialValue
                    val valueChangePercentage = if (initialValue == 0.0) 0.0 else {
                        (valueChange / initialValue) * 100
                    }
                    
                    Text(
                        text = "${currency.symbol}${String.format("%.2f", totalValue)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${if (valueChange >= 0) "+" else ""}${currency.symbol}${String.format("%.2f", valueChange)} (${String.format("%.2f", valueChangePercentage)}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (valueChange >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
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
        }
    }
}

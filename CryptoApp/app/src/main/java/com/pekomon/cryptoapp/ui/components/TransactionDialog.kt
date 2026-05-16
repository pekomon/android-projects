package com.pekomon.cryptoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pekomon.cryptoapp.data.Transaction
import com.pekomon.cryptoapp.data.TransactionType
import androidx.compose.foundation.layout.Arrangement
import com.pekomon.cryptoapp.core.formatting.DisplayFormatters
import com.pekomon.cryptoapp.data.Currency

@Composable
fun TransactionDialog(
    cryptoName: String,
    transactions: List<Transaction>,
    currentPrice: Double,
    initialPrice: Double,
    currency: Currency,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$cryptoName Transactions") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val priceChange = currentPrice - initialPrice
                val priceChangePercentage = if (initialPrice == 0.0) 0.0 else {
                    (priceChange / initialPrice) * 100
                }
                
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Performance",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Initial price: ${DisplayFormatters.currencyAmount(initialPrice, currency)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Current price: ${DisplayFormatters.currencyAmount(currentPrice, currency)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Change: ${DisplayFormatters.signedCurrencyAmount(priceChange, currency)} (${DisplayFormatters.percentage(priceChangePercentage)})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (priceChange >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.titleMedium
                )
                
                LazyColumn {
                    items(transactions.sortedByDescending { it.dateTime }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            currency = currency
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    currency: Currency
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when(transaction.type) {
                        TransactionType.BUY -> "Bought"
                        TransactionType.SELL -> "Sold"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = when(transaction.type) {
                        TransactionType.BUY -> MaterialTheme.colorScheme.tertiary
                        TransactionType.SELL -> MaterialTheme.colorScheme.error
                    }
                )
                Text(
                    text = DisplayFormatters.dateTime(transaction.dateTime),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${transaction.amount} units",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = DisplayFormatters.currencyAmount(transaction.price, currency),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

package com.example.pekomon.cryptoapp.ui.screens

import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.pekomon.cryptoapp.data.CryptoListItem
import com.example.pekomon.cryptoapp.data.Currency
import com.example.pekomon.cryptoapp.data.UserCrypto
import com.example.pekomon.cryptoapp.ui.CryptoViewModel
import com.example.pekomon.cryptoapp.ui.components.QuickAddDialog

@Composable
fun PortfolioScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    var editingCrypto by remember { mutableStateOf<UserCrypto?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Portfolio",
            style = MaterialTheme.typography.headlineMedium
        )
        
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
            }
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(viewModel.userCryptos) { userCrypto ->
                val crypto = viewModel.availableCryptos.find { it.id == userCrypto.cryptoId }
                val cryptoInfo = viewModel.getCryptoInfo(userCrypto.cryptoId)
                
                if (crypto != null) {
                    PortfolioItem(
                        cryptoName = crypto.name,
                        cryptoSymbol = crypto.symbol.uppercase(),
                        amount = userCrypto.amount,
                        value = (cryptoInfo?.currentPrice ?: 0.0) * userCrypto.amount,
                        currency = viewModel.selectedCurrency,
                        onEdit = { editingCrypto = userCrypto },
                        onDelete = { viewModel.removeUserCrypto(userCrypto.cryptoId) }
                    )
                }
            }
        }
    }
    
    editingCrypto?.let { crypto ->
        val cryptoName = viewModel.availableCryptos.find { it.id == crypto.cryptoId }?.name ?: "Unknown"
        QuickAddDialog(
            cryptoName = cryptoName,
            onDismiss = { editingCrypto = null },
            onConfirm = { amount ->
                viewModel.updateUserCrypto(crypto.cryptoId, amount)
                editingCrypto = null
            }
        )
    }
}

@Composable
private fun PortfolioItem(
    cryptoName: String,
    cryptoSymbol: String,
    amount: Double,
    value: Double,
    currency: Currency,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cryptoName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$cryptoSymbol • $amount",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${currency.symbol}%.2f".format(value),
                    style = MaterialTheme.typography.titleMedium
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
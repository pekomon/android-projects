package com.pekomon.cryptoapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pekomon.cryptoapp.core.formatting.DisplayFormatters
import com.pekomon.cryptoapp.data.Currency
import java.time.LocalDateTime
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme

@Composable
fun QuickAddDialog(
    cryptoName: String,
    currentPrice: Double,
    currency: Currency,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, price: Double, dateTime: LocalDateTime) -> Unit,
    initialAmount: Double? = null,
    initialPrice: Double = currentPrice,
    title: String = "Add $cryptoName",
    confirmLabel: String = "Add"
) {
    var amount by remember { mutableStateOf(initialAmount?.toString().orEmpty()) }
    var price by remember { mutableStateOf(initialPrice.toString()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price per unit (${currency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Text("Set Date")
                    }
                    OutlinedButton(onClick = { showTimePicker = true }) {
                        Text("Set Time")
                    }
                }
                
                Text(
                    text = "Purchase time: ${DisplayFormatters.dateTime(selectedDateTime)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let { amountValue ->
                        price.toDoubleOrNull()?.let { priceValue ->
                            onConfirm(amountValue, priceValue, selectedDateTime)
                        }
                    }
                },
                enabled = amount.toDoubleOrNull() != null && price.toDoubleOrNull() != null
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                selectedDateTime = selectedDateTime.with(date)
                showDatePicker = false
            }
        )
    }
    
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = { time ->
                selectedDateTime = selectedDateTime.with(time)
                showTimePicker = false
            }
        )
    }
}

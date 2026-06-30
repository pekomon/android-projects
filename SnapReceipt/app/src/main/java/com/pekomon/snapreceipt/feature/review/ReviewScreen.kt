package com.pekomon.snapreceipt.feature.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pekomon.snapreceipt.feature.capture.CaptureUiState

@Composable
fun ReviewScreen(
    uiState: CaptureUiState,
    onBackToCapture: () -> Unit,
    onMerchantNameChange: (String) -> Unit,
    onTransactionDateChange: (String) -> Unit,
    onTotalAmountChange: (String) -> Unit,
    onCurrencyCodeChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val reviewForm = uiState.reviewForm
    if (reviewForm == null || uiState.draft == null) {
        ReviewEmptyState(
            onBackToCapture = onBackToCapture,
            modifier = modifier,
            contentPadding = contentPadding
        )
        return
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "REVIEW",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Review parsed receipt fields before save.",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Correct anything OCR or parsing got wrong. This draft is editable, and incomplete values stay visible instead of being silently discarded.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = reviewForm.merchantName,
                onValueChange = onMerchantNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Merchant") },
                singleLine = true
            )
            OutlinedTextField(
                value = reviewForm.transactionDate,
                onValueChange = onTransactionDateChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date") },
                supportingText = { Text("Use dd/MM/yyyy") },
                singleLine = true
            )
            OutlinedTextField(
                value = reviewForm.totalAmount,
                onValueChange = onTotalAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Total") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = reviewForm.currencyCode,
                onValueChange = onCurrencyCodeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Currency") },
                supportingText = { Text("Use codes like EUR, USD, GBP") },
                singleLine = true
            )
            OutlinedTextField(
                value = reviewForm.notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 3
            )

            Text(
                text = reviewValidationText(uiState),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "OCR preview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = reviewForm.rawOcrPreview.ifBlank { "No OCR text available." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(onClick = onBackToCapture) {
                Text("Back to capture")
            }
        }
    }
}

@Composable
private fun ReviewEmptyState(
    onBackToCapture: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "No draft to review yet.",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Import a receipt first so OCR and parsing can populate the review form.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onBackToCapture) {
                Text("Open capture")
            }
        }
    }
}

private fun reviewValidationText(uiState: CaptureUiState): String {
    val draft = uiState.draft ?: return "Draft unavailable."
    return if (draft.isReadyToSave) {
        "Draft has the required merchant, date, total, and currency fields."
    } else {
        "Draft is still incomplete. Merchant, date, total, and currency all need valid values."
    }
}

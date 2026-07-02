package com.pekomon.snapreceipt.feature.detail

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pekomon.snapreceipt.core.formatting.formattedDate
import com.pekomon.snapreceipt.core.formatting.formattedTotal
import com.pekomon.snapreceipt.domain.model.Receipt

@Composable
fun ReceiptDetailScreen(
    uiState: ReceiptDetailUiState,
    onBack: () -> Unit,
    onDeleteReceipt: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.wasDeleted) {
        if (uiState.wasDeleted) {
            onBack()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.receipt != null -> {
                    ReceiptDetailContent(
                        receipt = uiState.receipt,
                        errorMessage = uiState.errorMessage,
                        isDeleting = uiState.isDeleting,
                        onBack = onBack,
                        onDelete = { showDeleteConfirmation = true }
                    )
                }

                else -> {
                    DetailMissingState(
                        errorMessage = uiState.errorMessage,
                        onBack = onBack
                    )
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete receipt?") },
            text = { Text("This removes the saved receipt and deletes its stored image file from local app storage.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteReceipt()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReceiptDetailContent(
    receipt: Receipt,
    errorMessage: String?,
    isDeleting: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "DETAIL",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = receipt.merchantName,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Saved locally on ${receipt.formattedDate()} for ${receipt.formattedTotal()}.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AsyncImage(
            model = Uri.parse(receipt.image.localPath),
            contentDescription = receipt.merchantName,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(28.dp)),
            contentScale = ContentScale.Crop
        )

        DetailFieldCard("Merchant", receipt.merchantName)
        DetailFieldCard("Date", receipt.formattedDate())
        DetailFieldCard("Total", receipt.formattedTotal())
        DetailFieldCard("Source", receipt.image.source.name.replace('_', ' '))
        if (receipt.notes.isNotBlank()) {
            DetailFieldCard("Notes", receipt.notes)
        }
        DetailFieldCard(
            "OCR preview",
            receipt.rawOcrText.ifBlank { "No OCR text was stored for this receipt." }
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onDelete,
            enabled = !isDeleting
        ) {
            Text(if (isDeleting) "Deleting..." else "Delete receipt")
        }

        OutlinedButton(onClick = onBack) {
            Text("Back to receipts")
        }
    }
}

@Composable
private fun DetailFieldCard(
    label: String,
    value: String
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DetailMissingState(
    errorMessage: String?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Receipt unavailable",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = errorMessage ?: "The requested receipt could not be loaded.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = onBack) {
            Text("Back to receipts")
        }
    }
}

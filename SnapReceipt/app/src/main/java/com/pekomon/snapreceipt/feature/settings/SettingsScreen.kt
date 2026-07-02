package com.pekomon.snapreceipt.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onDefaultCurrencySelected: (ReceiptCurrency) -> Unit,
    onImageCompressionQualityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
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
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Local defaults",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "These settings persist locally and feed the active receipt workflow. Default currency fills parser gaps, and image quality is used when stored images are written into app-private storage.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SettingsCard(
                        title = "Default currency",
                        body = "Used when OCR text does not clearly identify a currency code."
                    ) {
                        CurrencyChips(
                            selectedCurrency = uiState.settings.defaultCurrency,
                            onCurrencySelected = onDefaultCurrencySelected
                        )
                    }
                    SettingsCard(
                        title = "JPEG quality",
                        body = "Applied when imported images are normalized into local storage for saved receipts."
                    ) {
                        Text(
                            text = "${uiState.settings.imageCompressionQuality}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = uiState.settings.imageCompressionQuality.toFloat(),
                            onValueChange = { onImageCompressionQualityChange(it.toInt()) },
                            valueRange = 40f..100f
                        )
                    }
                    SettingsCard(
                        title = "Local storage summary",
                        body = "Saved receipts stay on-device. Deleting a receipt also removes its stored image file."
                    ) {
                        Text(
                            text = "${uiState.savedReceiptCount} saved receipt(s)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    body: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun CurrencyChips(
    selectedCurrency: ReceiptCurrency,
    onCurrencySelected: (ReceiptCurrency) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ReceiptCurrency.entries.chunked(3).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { currency ->
                    FilterChip(
                        selected = currency == selectedCurrency,
                        onClick = { onCurrencySelected(currency) },
                        label = { Text(currency.code) }
                    )
                }
            }
        }
    }
}

package com.pekomon.cryptoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pekomon.cryptoapp.ui.theme.CryptoSpacing

@Composable
fun StateMessageCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    CommonCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CryptoSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(CryptoSpacing.medium)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

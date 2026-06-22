package com.pekomon.cryptoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pekomon.cryptoapp.core.formatting.DisplayFormatters
import com.pekomon.cryptoapp.ui.AssetMetadataSource
import com.pekomon.cryptoapp.ui.MarketLoadState
import com.pekomon.cryptoapp.ui.theme.CryptoSpacing

@Composable
fun MarketStatusCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    marketLoadState: MarketLoadState? = null,
    assetMetadataSource: AssetMetadataSource? = null,
    isLoading: Boolean = false,
    trailingContent: @Composable (RowScope.() -> Unit)? = null
) {
    CommonCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CryptoSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(CryptoSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CryptoSpacing.xSmall)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                when (marketLoadState) {
                    is MarketLoadState.Content -> {
                        Text(
                            text = DisplayFormatters.updateTime(marketLoadState.lastUpdated),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (marketLoadState.isStale || marketLoadState.message != null) {
                            Text(
                                text = marketLoadState.message ?: "Using last successful prices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    is MarketLoadState.Error -> {
                        Text(
                            text = marketLoadState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    MarketLoadState.Loading -> {
                        Text(
                            text = "Refreshing market data…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    MarketLoadState.Idle, null -> Unit
                }
                assetMetadataSource?.let { source ->
                    Text(
                        text = source.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator()
            }

            trailingContent?.invoke(this)
        }
    }
}

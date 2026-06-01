package com.pekomon.cryptoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pekomon.cryptoapp.core.formatting.DisplayFormatters
import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import com.pekomon.cryptoapp.ui.theme.CryptoSpacing

@Composable
fun CryptoListItemRow(
    crypto: CryptoAsset,
    currentPrice: Double?,
    priceChangePercentage: Double,
    currency: Currency,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    CommonCard(
        onClick = onQuickAdd,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(CryptoSpacing.large)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CryptoSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1.15f),
                verticalArrangement = Arrangement.spacedBy(CryptoSpacing.xSmall)
            ) {
                Text(
                    text = crypto.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = crypto.symbol.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CryptoSpacing.xSmall)
            ) {
                if (currentPrice == null) {
                    Text(
                        text = DisplayFormatters.UNAVAILABLE,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No ${currency.name} quote",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = DisplayFormatters.currencyAmount(currentPrice, currency),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = DisplayFormatters.signedPercentage(priceChangePercentage),
                        color = if (priceChangePercentage >= 0) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(CryptoSpacing.xSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onQuickAdd()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Quick add to portfolio",
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                
                IconButton(
                    onClick = {
                        onFavoriteClick()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

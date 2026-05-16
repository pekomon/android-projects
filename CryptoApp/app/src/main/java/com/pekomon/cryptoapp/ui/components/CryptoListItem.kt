import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pekomon.cryptoapp.data.CryptoListItem
import com.pekomon.cryptoapp.data.Currency

@Composable
fun CryptoListItemRow(
    crypto: CryptoListItem,
    currentPrice: Double,
    priceChangePercentage: Double,
    currency: Currency,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
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
                    text = crypto.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = crypto.symbol.uppercase(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${currency.symbol}${currentPrice}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${if (priceChangePercentage >= 0) "+" else ""}${String.format("%.2f", priceChangePercentage)}%",
                    color = if (priceChangePercentage >= 0) Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onQuickAdd) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Quick add to portfolio",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites"
                    )
                }
            }
        }
    }
} 
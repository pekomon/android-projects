package com.pekomon.cryptoapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pekomon.cryptoapp.ui.theme.CryptoElevation
import com.pekomon.cryptoapp.ui.theme.CryptoShapes

@Composable
fun CommonCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = CryptoShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = CryptoElevation.card,
            pressedElevation = CryptoElevation.selected
        )
    ) {
        content()
    }
}

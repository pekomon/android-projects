package com.pekomon.snapreceipt.feature.receipts

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pekomon.snapreceipt.R
import com.pekomon.snapreceipt.ui.components.PlaceholderScreenLayout

@Composable
fun ReceiptsPlaceholderScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    PlaceholderScreenLayout(
        modifier = modifier,
        contentPadding = contentPadding,
        eyebrow = stringResource(R.string.receipts_eyebrow),
        title = stringResource(R.string.receipts_title),
        subtitle = stringResource(R.string.receipts_subtitle),
        primaryCardTitle = stringResource(R.string.receipts_card_primary_title),
        primaryCardBody = stringResource(R.string.receipts_card_primary_body),
        secondaryCardTitle = stringResource(R.string.receipts_card_secondary_title),
        secondaryCardBody = stringResource(R.string.receipts_card_secondary_body)
    )
}

package com.pekomon.snapreceipt.feature.capture

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pekomon.snapreceipt.R
import com.pekomon.snapreceipt.ui.components.PlaceholderScreenLayout

@Composable
fun CapturePlaceholderScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    PlaceholderScreenLayout(
        modifier = modifier,
        contentPadding = contentPadding,
        eyebrow = stringResource(R.string.capture_eyebrow),
        title = stringResource(R.string.capture_title),
        subtitle = stringResource(R.string.capture_subtitle),
        primaryCardTitle = stringResource(R.string.capture_card_primary_title),
        primaryCardBody = stringResource(R.string.capture_card_primary_body),
        secondaryCardTitle = stringResource(R.string.capture_card_secondary_title),
        secondaryCardBody = stringResource(R.string.capture_card_secondary_body)
    )
}

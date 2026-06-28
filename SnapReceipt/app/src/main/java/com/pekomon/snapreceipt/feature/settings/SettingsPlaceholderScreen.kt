package com.pekomon.snapreceipt.feature.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pekomon.snapreceipt.R
import com.pekomon.snapreceipt.ui.components.PlaceholderScreenLayout

@Composable
fun SettingsPlaceholderScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    PlaceholderScreenLayout(
        modifier = modifier,
        contentPadding = contentPadding,
        eyebrow = stringResource(R.string.settings_eyebrow),
        title = stringResource(R.string.settings_title),
        subtitle = stringResource(R.string.settings_subtitle),
        primaryCardTitle = stringResource(R.string.settings_card_primary_title),
        primaryCardBody = stringResource(R.string.settings_card_primary_body),
        secondaryCardTitle = stringResource(R.string.settings_card_secondary_title),
        secondaryCardBody = stringResource(R.string.settings_card_secondary_body)
    )
}

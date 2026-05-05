package com.example.pekomon.weatherly.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pekomon.weatherly.core.model.AppAppearanceMode
import com.example.pekomon.weatherly.core.model.AppSettings
import com.example.pekomon.weatherly.core.model.TemperatureUnit
import com.example.pekomon.weatherly.core.model.WindSpeedUnit
import com.example.pekomon.weatherly.ui.theme.WeatherlyTheme

@Composable
fun SettingsRoute(
    contentPadding: PaddingValues,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(context = LocalContext.current.applicationContext),
    ),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    SettingsScreen(
        settings = settings,
        onTemperatureUnitSelected = viewModel::setTemperatureUnit,
        onWindSpeedUnitSelected = viewModel::setWindSpeedUnit,
        onAppearanceModeSelected = viewModel::setAppearanceMode,
        modifier = Modifier.padding(contentPadding),
    )
}

@Composable
internal fun SettingsScreen(
    settings: AppSettings,
    onTemperatureUnitSelected: (TemperatureUnit) -> Unit,
    onWindSpeedUnitSelected: (WindSpeedUnit) -> Unit,
    onAppearanceModeSelected: (AppAppearanceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Adjust units and appearance. Changes apply across Home, Search, and Favorites.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                SettingsSection(
                    title = "Temperature",
                    description = "Choose how temperatures are displayed throughout the app.",
                ) {
                    TemperatureUnit.entries.forEach { unit ->
                        SettingsChoiceRow(
                            label = unit.settingsLabel,
                            selected = settings.temperatureUnit == unit,
                            onClick = { onTemperatureUnitSelected(unit) },
                        )
                    }
                }
            }
            item {
                SettingsSection(
                    title = "Wind Speed",
                    description = "Switch between metric and imperial wind speed formats.",
                ) {
                    WindSpeedUnit.entries.forEach { unit ->
                        SettingsChoiceRow(
                            label = unit.settingsLabel,
                            selected = settings.windSpeedUnit == unit,
                            onClick = { onWindSpeedUnitSelected(unit) },
                        )
                    }
                }
            }
            item {
                SettingsSection(
                    title = "Appearance",
                    description = "Use the system theme or force a light or dark presentation.",
                ) {
                    AppAppearanceMode.entries.forEach { mode ->
                        SettingsChoiceRow(
                            label = appearanceLabel(mode),
                            selected = settings.appearanceMode == mode,
                            onClick = { onAppearanceModeSelected(mode) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
    }
}

private fun appearanceLabel(mode: AppAppearanceMode): String = when (mode) {
    AppAppearanceMode.System -> "System"
    AppAppearanceMode.Light -> "Light"
    AppAppearanceMode.Dark -> "Dark"
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    WeatherlyTheme {
        SettingsScreen(
            settings = AppSettings(),
            onTemperatureUnitSelected = {},
            onWindSpeedUnitSelected = {},
            onAppearanceModeSelected = {},
        )
    }
}

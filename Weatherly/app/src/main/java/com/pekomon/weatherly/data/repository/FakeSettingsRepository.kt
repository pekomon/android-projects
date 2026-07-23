package com.pekomon.weatherly.data.repository

import com.pekomon.weatherly.core.model.AppAppearanceMode
import com.pekomon.weatherly.core.model.AppSettings
import com.pekomon.weatherly.core.model.TemperatureUnit
import com.pekomon.weatherly.core.model.WindSpeedUnit
import com.pekomon.weatherly.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsRepository : SettingsRepository {
    private val settingsState = MutableStateFlow(AppSettings())

    override val settings: StateFlow<AppSettings> = settingsState.asStateFlow()

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        settingsState.value = settingsState.value.copy(temperatureUnit = unit)
    }

    override suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
        settingsState.value = settingsState.value.copy(windSpeedUnit = unit)
    }

    override suspend fun setAppearanceMode(mode: AppAppearanceMode) {
        settingsState.value = settingsState.value.copy(appearanceMode = mode)
    }
}

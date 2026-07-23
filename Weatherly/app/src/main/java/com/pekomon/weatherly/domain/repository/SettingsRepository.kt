package com.pekomon.weatherly.domain.repository

import com.pekomon.weatherly.core.model.AppAppearanceMode
import com.pekomon.weatherly.core.model.AppSettings
import com.pekomon.weatherly.core.model.TemperatureUnit
import com.pekomon.weatherly.core.model.WindSpeedUnit
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<AppSettings>

    suspend fun setTemperatureUnit(unit: TemperatureUnit)

    suspend fun setWindSpeedUnit(unit: WindSpeedUnit)

    suspend fun setAppearanceMode(mode: AppAppearanceMode)
}

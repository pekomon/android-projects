package com.example.pekomon.weatherly.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.pekomon.weatherly.core.model.AppAppearanceMode
import com.example.pekomon.weatherly.core.model.AppSettings
import com.example.pekomon.weatherly.core.model.TemperatureUnit
import com.example.pekomon.weatherly.core.model.WindSpeedUnit
import com.example.pekomon.weatherly.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DataStoreSettingsRepository(
    context: Context,
) : SettingsRepository {
    private val dataStore = context.weatherlyDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val settings: StateFlow<AppSettings> = dataStore.data
        .map { preferences ->
            AppSettings(
                temperatureUnit = preferences[TEMPERATURE_UNIT_KEY]
                    ?.let(TemperatureUnit::valueOf)
                    ?: AppSettings().temperatureUnit,
                windSpeedUnit = preferences[WIND_SPEED_UNIT_KEY]
                    ?.let(WindSpeedUnit::valueOf)
                    ?: AppSettings().windSpeedUnit,
                appearanceMode = preferences[APPEARANCE_MODE_KEY]
                    ?.let(AppAppearanceMode::valueOf)
                    ?: AppSettings().appearanceMode,
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings(),
        )

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        dataStore.edit { preferences ->
            preferences[TEMPERATURE_UNIT_KEY] = unit.name
        }
    }

    override suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
        dataStore.edit { preferences ->
            preferences[WIND_SPEED_UNIT_KEY] = unit.name
        }
    }

    override suspend fun setAppearanceMode(mode: AppAppearanceMode) {
        dataStore.edit { preferences ->
            preferences[APPEARANCE_MODE_KEY] = mode.name
        }
    }

    private companion object {
        val TEMPERATURE_UNIT_KEY = stringPreferencesKey("temperature_unit")
        val WIND_SPEED_UNIT_KEY = stringPreferencesKey("wind_speed_unit")
        val APPEARANCE_MODE_KEY = stringPreferencesKey("appearance_mode")
    }
}

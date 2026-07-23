package com.pekomon.weatherly.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pekomon.weatherly.core.model.AppAppearanceMode
import com.pekomon.weatherly.core.model.AppSettings
import com.pekomon.weatherly.core.model.TemperatureUnit
import com.pekomon.weatherly.core.model.WindSpeedUnit
import com.pekomon.weatherly.data.repository.DataStoreSettingsRepository
import com.pekomon.weatherly.domain.repository.SettingsRepository
import com.pekomon.weatherly.widget.WeatherlyWidgetUpdater
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val context: Context,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch {
            settingsRepository.setTemperatureUnit(unit)
            WeatherlyWidgetUpdater.refresh(context)
        }
    }

    fun setWindSpeedUnit(unit: WindSpeedUnit) {
        viewModelScope.launch {
            settingsRepository.setWindSpeedUnit(unit)
            WeatherlyWidgetUpdater.refresh(context)
        }
    }

    fun setAppearanceMode(mode: AppAppearanceMode) {
        viewModelScope.launch {
            settingsRepository.setAppearanceMode(mode)
            WeatherlyWidgetUpdater.refresh(context)
        }
    }

    companion object {
        fun factory(
            context: Context,
            settingsRepository: SettingsRepository = DataStoreSettingsRepository(context.applicationContext),
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(settingsRepository, context.applicationContext) as T
            }
        }
    }
}

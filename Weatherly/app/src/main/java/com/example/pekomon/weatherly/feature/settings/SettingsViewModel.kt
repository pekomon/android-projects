package com.example.pekomon.weatherly.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pekomon.weatherly.core.model.AppAppearanceMode
import com.example.pekomon.weatherly.core.model.AppSettings
import com.example.pekomon.weatherly.core.model.TemperatureUnit
import com.example.pekomon.weatherly.core.model.WindSpeedUnit
import com.example.pekomon.weatherly.data.repository.DataStoreSettingsRepository
import com.example.pekomon.weatherly.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch {
            settingsRepository.setTemperatureUnit(unit)
        }
    }

    fun setWindSpeedUnit(unit: WindSpeedUnit) {
        viewModelScope.launch {
            settingsRepository.setWindSpeedUnit(unit)
        }
    }

    fun setAppearanceMode(mode: AppAppearanceMode) {
        viewModelScope.launch {
            settingsRepository.setAppearanceMode(mode)
        }
    }

    companion object {
        fun factory(
            context: Context,
            settingsRepository: SettingsRepository = DataStoreSettingsRepository(context.applicationContext),
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(settingsRepository) as T
            }
        }
    }
}

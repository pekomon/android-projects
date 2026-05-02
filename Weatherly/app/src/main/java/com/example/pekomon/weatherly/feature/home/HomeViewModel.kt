package com.example.pekomon.weatherly.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pekomon.weatherly.core.model.WeatherDetails
import com.example.pekomon.weatherly.data.repository.OpenMeteoWeatherRepository
import com.example.pekomon.weatherly.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object PermissionRequired : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Loaded(val weatherDetails: WeatherDetails) : HomeUiState
}

class HomeViewModel(
    private val weatherRepository: WeatherRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            runCatching {
                weatherRepository.getCurrentLocationWeather()
            }.onSuccess { weatherDetails ->
                _uiState.value = HomeUiState.Loaded(weatherDetails)
            }.onFailure { throwable ->
                _uiState.value = HomeUiState.Error(
                    message = throwable.message ?: "Unable to load the latest weather.",
                )
            }
        }
    }

    fun showPermissionRequired() {
        _uiState.value = HomeUiState.PermissionRequired
    }

    companion object {
        fun factory(
            weatherRepository: WeatherRepository = OpenMeteoWeatherRepository(),
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(weatherRepository) as T
            }
        }
    }
}

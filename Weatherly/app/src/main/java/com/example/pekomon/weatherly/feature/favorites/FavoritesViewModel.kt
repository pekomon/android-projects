package com.example.pekomon.weatherly.feature.favorites

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.core.model.WeatherDetails
import com.example.pekomon.weatherly.data.repository.DataStoreFavoritesRepository
import com.example.pekomon.weatherly.data.repository.OpenMeteoWeatherRepository
import com.example.pekomon.weatherly.domain.repository.FavoritesRepository
import com.example.pekomon.weatherly.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<Location> = emptyList(),
    val selectedLocationWeather: WeatherDetails? = null,
    val isLoadingSelection: Boolean = false,
    val errorMessage: String? = null,
)

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository.favorites.collectLatest { favorites ->
                _uiState.update { current ->
                    current.copy(
                        favorites = favorites,
                        selectedLocationWeather = current.selectedLocationWeather?.takeIf { details ->
                            favorites.any { it.id == details.location.id }
                        },
                    )
                }
            }
        }
    }

    fun selectLocation(location: Location) {
        _uiState.update { current ->
            current.copy(
                isLoadingSelection = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            runCatching {
                weatherRepository.getWeatherDetails(location)
            }.onSuccess { weatherDetails ->
                _uiState.update { current ->
                    current.copy(
                        isLoadingSelection = false,
                        selectedLocationWeather = weatherDetails,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { current ->
                    current.copy(
                        isLoadingSelection = false,
                        errorMessage = throwable.message ?: "Unable to load weather for ${location.name}.",
                    )
                }
            }
        }
    }

    fun removeFavorite(locationId: String) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(locationId)
        }
    }

    companion object {
        fun factory(
            context: Context,
            favoritesRepository: FavoritesRepository = DataStoreFavoritesRepository(context.applicationContext),
            weatherRepository: WeatherRepository = OpenMeteoWeatherRepository(),
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FavoritesViewModel(favoritesRepository, weatherRepository) as T
            }
        }
    }
}

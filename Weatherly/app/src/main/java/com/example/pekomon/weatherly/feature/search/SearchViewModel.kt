package com.example.pekomon.weatherly.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.core.model.WeatherDetails
import com.example.pekomon.weatherly.data.repository.OpenMeteoLocationSearchRepository
import com.example.pekomon.weatherly.data.repository.OpenMeteoWeatherRepository
import com.example.pekomon.weatherly.domain.repository.LocationSearchRepository
import com.example.pekomon.weatherly.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val isLoadingSelection: Boolean = false,
    val results: List<Location> = emptyList(),
    val selectedLocationWeather: WeatherDetails? = null,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
)

class SearchViewModel(
    private val locationSearchRepository: LocationSearchRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun updateQuery(query: String) {
        _uiState.update { current ->
            current.copy(
                query = query,
                errorMessage = null,
                hasSearched = current.hasSearched && query.trim() == current.query.trim(),
            )
        }
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.length < 2) {
            _uiState.update { current ->
                current.copy(
                    results = emptyList(),
                    selectedLocationWeather = null,
                    hasSearched = false,
                    isSearching = false,
                    errorMessage = "Enter at least 2 characters to search for a place.",
                )
            }
            return
        }

        _uiState.update { current ->
            current.copy(
                isSearching = true,
                errorMessage = null,
                hasSearched = true,
                selectedLocationWeather = null,
            )
        }

        viewModelScope.launch {
            runCatching {
                locationSearchRepository.searchLocations(query)
            }.onSuccess { results ->
                _uiState.update { current ->
                    current.copy(
                        isSearching = false,
                        results = results,
                        errorMessage = if (results.isEmpty()) {
                            "No places matched \"$query\"."
                        } else {
                            null
                        },
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { current ->
                    current.copy(
                        isSearching = false,
                        results = emptyList(),
                        errorMessage = throwable.message ?: "Unable to search locations right now.",
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

    companion object {
        fun factory(
            locationSearchRepository: LocationSearchRepository = OpenMeteoLocationSearchRepository(),
            weatherRepository: WeatherRepository = OpenMeteoWeatherRepository(),
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchViewModel(locationSearchRepository, weatherRepository) as T
            }
        }
    }
}

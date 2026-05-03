package com.example.pekomon.weatherly.feature.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pekomon.weatherly.core.model.Location
import com.example.pekomon.weatherly.core.model.WeatherDetails
import com.example.pekomon.weatherly.data.repository.DataStoreFavoritesRepository
import com.example.pekomon.weatherly.data.repository.OpenMeteoLocationSearchRepository
import com.example.pekomon.weatherly.data.repository.OpenMeteoWeatherRepository
import com.example.pekomon.weatherly.domain.repository.FavoritesRepository
import com.example.pekomon.weatherly.domain.repository.LocationSearchRepository
import com.example.pekomon.weatherly.domain.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val isLoadingSelection: Boolean = false,
    val results: List<Location> = emptyList(),
    val selectedLocationWeather: WeatherDetails? = null,
    val favoriteLocationIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
)

class SearchViewModel(
    private val locationSearchRepository: LocationSearchRepository,
    private val weatherRepository: WeatherRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null
    private var searchRequestToken: Int = 0

    init {
        viewModelScope.launch {
            favoritesRepository.favorites.collectLatest { favorites ->
                _uiState.update { current ->
                    current.copy(
                        favoriteLocationIds = favorites.mapTo(linkedSetOf()) { it.id },
                    )
                }
            }
        }
    }

    fun updateQuery(query: String) {
        val normalizedQuery = query.trim()
        searchJob?.cancel()
        searchRequestToken += 1

        _uiState.update { current ->
            current.copy(
                query = query,
                isSearching = normalizedQuery.length >= MIN_QUERY_LENGTH,
                results = emptyList(),
                selectedLocationWeather = null,
                errorMessage = null,
                hasSearched = false,
            )
        }

        if (normalizedQuery.length < MIN_QUERY_LENGTH) {
            _uiState.update { current ->
                current.copy(isSearching = false)
            }
            return
        }

        scheduleSearch(
            query = normalizedQuery,
            debounceMillis = SEARCH_DEBOUNCE_MILLIS,
        )
    }

    fun clearQuery() {
        searchJob?.cancel()
        searchRequestToken += 1
        _uiState.update { current ->
            current.copy(
                query = "",
                isSearching = false,
                isLoadingSelection = false,
                results = emptyList(),
                selectedLocationWeather = null,
                errorMessage = null,
                hasSearched = false,
            )
        }
    }

    fun searchNow() {
        val query = _uiState.value.query.trim()
        searchJob?.cancel()
        searchRequestToken += 1

        if (query.length < MIN_QUERY_LENGTH) {
            _uiState.update { current ->
                current.copy(
                    results = emptyList(),
                    selectedLocationWeather = null,
                    hasSearched = false,
                    isSearching = false,
                    errorMessage = "Enter at least $MIN_QUERY_LENGTH characters to search for a place.",
                )
            }
            return
        }

        scheduleSearch(query = query, debounceMillis = 0L)
    }

    private fun scheduleSearch(
        query: String,
        debounceMillis: Long,
    ) {
        val requestToken = searchRequestToken
        searchJob = viewModelScope.launch {
            if (debounceMillis > 0) {
                delay(debounceMillis)
            }

            _uiState.update { current ->
                current.copy(
                    isSearching = true,
                    errorMessage = null,
                    hasSearched = true,
                    selectedLocationWeather = null,
                )
            }

            runCatching {
                locationSearchRepository.searchLocations(query)
            }.onSuccess { results ->
                if (!isCurrentSearch(requestToken, query)) return@onSuccess
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
                if (!isCurrentSearch(requestToken, query)) return@onFailure
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

    private fun isCurrentSearch(
        requestToken: Int,
        query: String,
    ): Boolean = requestToken == searchRequestToken && _uiState.value.query.trim() == query

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

    fun toggleFavorite(location: Location) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(location)
        }
    }

    companion object {
        private const val MIN_QUERY_LENGTH = 2
        private const val SEARCH_DEBOUNCE_MILLIS = 350L

        fun factory(
            context: Context,
            locationSearchRepository: LocationSearchRepository = OpenMeteoLocationSearchRepository(),
            weatherRepository: WeatherRepository = OpenMeteoWeatherRepository(),
            favoritesRepository: FavoritesRepository = DataStoreFavoritesRepository(context.applicationContext),
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchViewModel(
                    locationSearchRepository = locationSearchRepository,
                    weatherRepository = weatherRepository,
                    favoritesRepository = favoritesRepository,
                ) as T
            }
        }
    }
}

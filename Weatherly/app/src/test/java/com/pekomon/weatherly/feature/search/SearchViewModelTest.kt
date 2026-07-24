package com.pekomon.weatherly.feature.search

import com.pekomon.weatherly.testing.FakeFavoritesRepository
import com.pekomon.weatherly.testing.FakeLocationSearchRepository
import com.pekomon.weatherly.testing.FakeWeatherRepository
import com.pekomon.weatherly.testing.MainDispatcherRule
import com.pekomon.weatherly.testing.secondTestLocation
import com.pekomon.weatherly.testing.testLocation
import com.pekomon.weatherly.testing.testWeatherDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun shortQueryDoesNotSearchUntilExplicitSearchShowsGuidance() {
        val searchRepository = FakeLocationSearchRepository()
        val viewModel = viewModel(searchRepository = searchRepository)

        viewModel.updateQuery("h")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(searchRepository.queries.isEmpty())
        assertFalse(viewModel.uiState.value.isSearching)

        viewModel.searchNow()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Enter at least 2 characters to search for a place.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun debouncedLatestQueryWins() {
        val searchRepository = FakeLocationSearchRepository().apply {
            resultFor("He", Result.success(listOf(testLocation)))
            resultFor("Hel", Result.success(listOf(secondTestLocation)))
        }
        val viewModel = viewModel(searchRepository = searchRepository)

        viewModel.updateQuery("He")
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(100)
        viewModel.updateQuery("Hel")
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(350)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("Hel"), searchRepository.queries)
        assertEquals(listOf(secondTestLocation), viewModel.uiState.value.results)
    }

    @Test
    fun searchResultEmptyAndFailureStatesAreExposed() {
        val searchRepository = FakeLocationSearchRepository().apply {
            resultFor("None", Result.success(emptyList()))
            resultFor("Fail", Result.failure(IllegalStateException("search offline")))
        }
        val viewModel = viewModel(searchRepository = searchRepository)

        viewModel.updateQuery("None")
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(350)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("No places matched \"None\".", viewModel.uiState.value.errorMessage)

        viewModel.updateQuery("Fail")
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(350)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("search offline", viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }

    @Test
    fun selectionSuccessFailureAndFavoriteToggleUpdateState() {
        val weather = testWeatherDetails(testLocation)
        val weatherRepository = FakeWeatherRepository(
            locationWeatherResults = ArrayDeque(
                listOf(
                    Result.success(weather),
                    Result.failure(IllegalStateException("detail offline")),
                ),
            ),
        )
        val favoritesRepository = FakeFavoritesRepository()
        val viewModel = viewModel(
            weatherRepository = weatherRepository,
            favoritesRepository = favoritesRepository,
        )
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectLocation(testLocation)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(weather, viewModel.uiState.value.selectedLocationWeather)

        viewModel.toggleFavorite(testLocation)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(setOf(testLocation.id), viewModel.uiState.value.favoriteLocationIds)

        viewModel.selectLocation(secondTestLocation)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("detail offline", viewModel.uiState.value.errorMessage)
    }

    private fun viewModel(
        searchRepository: FakeLocationSearchRepository = FakeLocationSearchRepository(),
        weatherRepository: FakeWeatherRepository = FakeWeatherRepository(),
        favoritesRepository: FakeFavoritesRepository = FakeFavoritesRepository(),
    ) = SearchViewModel(searchRepository, weatherRepository, favoritesRepository)
}

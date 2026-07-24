package com.pekomon.weatherly.feature.favorites

import com.pekomon.weatherly.testing.FakeFavoritesRepository
import com.pekomon.weatherly.testing.FakeWeatherRepository
import com.pekomon.weatherly.testing.MainDispatcherRule
import com.pekomon.weatherly.testing.secondTestLocation
import com.pekomon.weatherly.testing.testLocation
import com.pekomon.weatherly.testing.testWeatherDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun favoritesFlowStartsEmptyAndUpdatesWhenRepositoryChanges() {
        val favoritesRepository = FakeFavoritesRepository()
        val viewModel = FavoritesViewModel(favoritesRepository, FakeWeatherRepository())
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<com.pekomon.weatherly.core.model.Location>(), viewModel.uiState.value.favorites)

        kotlinx.coroutines.runBlocking {
            favoritesRepository.addFavorite(testLocation)
        }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(testLocation), viewModel.uiState.value.favorites)
    }

    @Test
    fun selectionSuccessAndFailureUpdateState() {
        val weather = testWeatherDetails(testLocation)
        val weatherRepository = FakeWeatherRepository(
            locationWeatherResults = ArrayDeque(
                listOf(
                    Result.success(weather),
                    Result.failure(IllegalStateException("favorite offline")),
                ),
            ),
        )
        val viewModel = FavoritesViewModel(FakeFavoritesRepository(listOf(testLocation)), weatherRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectLocation(testLocation)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(weather, viewModel.uiState.value.selectedLocationWeather)

        viewModel.selectLocation(secondTestLocation)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("favorite offline", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun removalClearsRemovedSelectedFavorite() {
        val favoritesRepository = FakeFavoritesRepository(listOf(testLocation))
        val viewModel = FavoritesViewModel(
            favoritesRepository,
            FakeWeatherRepository(locationWeatherResults = ArrayDeque(listOf(Result.success(testWeatherDetails(testLocation))))),
        )
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectLocation(testLocation)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.removeFavorite(testLocation.id)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<com.pekomon.weatherly.core.model.Location>(), viewModel.uiState.value.favorites)
        assertNull(viewModel.uiState.value.selectedLocationWeather)
    }
}

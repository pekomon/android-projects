package com.pekomon.weatherly.feature.home

import com.pekomon.weatherly.testing.FakeWeatherRepository
import com.pekomon.weatherly.testing.MainDispatcherRule
import com.pekomon.weatherly.testing.testWeatherDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialLoadShowsWeather() {
        val weather = testWeatherDetails()
        val viewModel = HomeViewModel(
            FakeWeatherRepository(currentWeatherResults = ArrayDeque(listOf(Result.success(weather)))),
        )

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(HomeUiState.Loaded(weather), viewModel.uiState.value)
    }

    @Test
    fun refreshFailureShowsErrorThenRetryCanSucceed() {
        val weather = testWeatherDetails()
        val repository = FakeWeatherRepository(
            currentWeatherResults = ArrayDeque(
                listOf(
                    Result.failure(IllegalStateException("offline")),
                    Result.success(weather),
                ),
            ),
        )
        val viewModel = HomeViewModel(repository)

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(HomeUiState.Error("offline"), viewModel.uiState.value)

        viewModel.refresh()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(HomeUiState.Loaded(weather), viewModel.uiState.value)
        assertEquals(2, repository.currentRequestCount)
    }

    @Test
    fun permissionStateCanBeShownWithoutRepositoryCall() {
        val viewModel = HomeViewModel(FakeWeatherRepository())
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.showPermissionRequired()

        assertTrue(viewModel.uiState.value is HomeUiState.PermissionRequired)
    }
}

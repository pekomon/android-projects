package com.pekomon.weatherly.feature.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pekomon.weatherly.core.model.AppSettings
import com.pekomon.weatherly.testing.composeTestLocation
import com.pekomon.weatherly.testing.composeWeatherDetails
import com.pekomon.weatherly.ui.theme.WeatherlyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FavoritesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersEmptyPopulatedLoadingSelectedAndRemoveAction() {
        var selectedId: String? = null
        var removedId: String? = null
        var uiState by mutableStateOf(FavoritesUiState())

        composeRule.setContent {
            WeatherlyTheme {
                FavoritesScreen(
                    uiState = uiState,
                    settings = AppSettings(),
                    onLocationSelected = { selectedId = it.id },
                    onRemoveFavorite = { removedId = it },
                )
            }
        }
        composeRule.onNodeWithText("No saved places yet. Add favorites from Search to build your shortlist.")
            .assertIsDisplayed()

        composeRule.runOnIdle {
            uiState = FavoritesUiState(favorites = listOf(composeTestLocation))
        }
        composeRule.onNodeWithText("Helsinki").performClick()
        assertEquals(composeTestLocation.id, selectedId)
        composeRule.onNodeWithContentDescription("Remove favorite").performClick()
        assertEquals(composeTestLocation.id, removedId)

        composeRule.runOnIdle {
            uiState = FavoritesUiState(isLoadingSelection = true)
        }
        composeRule.onNodeWithText("Loading weather for the selected favorite…").assertIsDisplayed()

        composeRule.runOnIdle {
            uiState = FavoritesUiState(
                favorites = listOf(composeTestLocation),
                selectedLocationWeather = composeWeatherDetails(composeTestLocation),
            )
        }
        composeRule.onNodeWithText("Selected Favorite").assertIsDisplayed()
        composeRule.onNodeWithText("Helsinki").assertIsDisplayed()
    }
}

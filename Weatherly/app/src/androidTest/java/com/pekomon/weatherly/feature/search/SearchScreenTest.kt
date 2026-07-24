package com.pekomon.weatherly.feature.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.pekomon.weatherly.core.model.AppSettings
import com.pekomon.weatherly.core.model.Location
import com.pekomon.weatherly.testing.composeSecondLocation
import com.pekomon.weatherly.testing.composeTestLocation
import com.pekomon.weatherly.testing.composeWeatherDetails
import com.pekomon.weatherly.ui.theme.WeatherlyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersShortQueryResultsErrorsSelectionAndFavoriteAction() {
        var selectedLocation: Location? = null
        var favoriteLocation: Location? = null
        var uiState by mutableStateOf(SearchUiState(query = "H"))

        composeRule.setContent {
            WeatherlyTheme {
                SearchScreen(
                    uiState = uiState,
                    settings = AppSettings(),
                    onQueryChange = {},
                    onSearch = {},
                    onClearQuery = {},
                    onLocationSelected = { selectedLocation = it },
                    onToggleFavorite = { favoriteLocation = it },
                )
            }
        }
        composeRule.onNodeWithText("Type at least 2 characters. Press search on the keyboard to run immediately.")
            .assertIsDisplayed()

        composeRule.runOnIdle {
            uiState = SearchUiState(
                query = "Hel",
                results = listOf(composeTestLocation),
                hasSearched = true,
            )
        }
        composeRule.onNodeWithText("Results").assertIsDisplayed()
        composeRule.onNodeWithText("Helsinki").performClick()
        assertEquals(composeTestLocation, selectedLocation)
        composeRule.onNodeWithContentDescription("Add to favorites").performClick()
        assertEquals(composeTestLocation, favoriteLocation)

        composeRule.runOnIdle {
            uiState = SearchUiState(
                query = "Xx",
                errorMessage = "No places matched \"Xx\".",
                hasSearched = true,
            )
        }
        composeRule.onNodeWithText("No places matched \"Xx\".").assertIsDisplayed()

        composeRule.runOnIdle {
            uiState = SearchUiState(isLoadingSelection = true)
        }
        composeRule.onNodeWithText("Loading forecast for the selected place…").assertIsDisplayed()

        composeRule.runOnIdle {
            uiState = SearchUiState(
                selectedLocationWeather = composeWeatherDetails(composeSecondLocation),
                favoriteLocationIds = setOf(composeSecondLocation.id),
            )
        }
        composeRule.onNodeWithText("Selected Place").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Turku, Finland").performScrollTo().assertIsDisplayed()
    }
}

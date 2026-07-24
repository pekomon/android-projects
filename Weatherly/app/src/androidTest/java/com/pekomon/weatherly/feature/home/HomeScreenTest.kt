package com.pekomon.weatherly.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.pekomon.weatherly.core.model.AppSettings
import com.pekomon.weatherly.testing.composeWeatherDetails
import com.pekomon.weatherly.ui.theme.WeatherlyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersLoadingErrorRetryAndLoadedContent() {
        var retryClicked = false
        var uiState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        composeRule.setContent {
            WeatherlyTheme {
                HomeScreen(
                    uiState = uiState,
                    settings = AppSettings(),
                    onRetry = { retryClicked = true },
                    onEnableLocation = {},
                )
            }
        }

        composeRule.onNodeWithText("Loading Local Weather").assertIsDisplayed()

        composeRule.runOnIdle {
            uiState = HomeUiState.Error("offline")
        }
        composeRule.onNodeWithText("Unable to Load Weather").assertIsDisplayed()
        composeRule.onNodeWithText("Try again").performClick()
        assertTrue(retryClicked)

        composeRule.runOnIdle {
            uiState = HomeUiState.Loaded(composeWeatherDetails())
        }
        composeRule.onNodeWithText("Helsinki, Uusimaa").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Partly cloudy in Helsinki").performScrollTo().assertIsDisplayed()
    }
}

package com.pekomon.weatherly.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pekomon.weatherly.ui.theme.WeatherlyTheme
import org.junit.Rule
import org.junit.Test

class WeatherlyAppTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun topLevelNavigationReachesAllTabs() {
        composeRule.setContent {
            WeatherlyTheme {
                WeatherlyApp(
                    homeContent = { _: PaddingValues -> Text("home_content") },
                    searchContent = { _: PaddingValues -> Text("search_content") },
                    favoritesContent = { _: PaddingValues -> Text("favorites_content") },
                    settingsContent = { _: PaddingValues -> Text("settings_content") },
                )
            }
        }

        composeRule.onNodeWithText("home_content").assertIsDisplayed()
        composeRule.onNodeWithText("Search").performClick()
        composeRule.onNodeWithText("search_content").assertIsDisplayed()
        composeRule.onNodeWithText("Favorites").performClick()
        composeRule.onNodeWithText("favorites_content").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("settings_content").assertIsDisplayed()
        composeRule.onNodeWithText("Home").performClick()
        composeRule.onNodeWithText("home_content").assertIsDisplayed()
    }
}

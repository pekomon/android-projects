package com.pekomon.weatherly.feature.settings

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.pekomon.weatherly.core.model.AppAppearanceMode
import com.pekomon.weatherly.core.model.AppSettings
import com.pekomon.weatherly.core.model.TemperatureUnit
import com.pekomon.weatherly.core.model.WindSpeedUnit
import com.pekomon.weatherly.ui.theme.WeatherlyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersChoicesAndInvokesCallbacks() {
        var temperatureUnit: TemperatureUnit? = null
        var windSpeedUnit: WindSpeedUnit? = null
        var appearanceMode: AppAppearanceMode? = null

        composeRule.setContent {
            WeatherlyTheme {
                SettingsScreen(
                    settings = AppSettings(),
                    onTemperatureUnitSelected = { temperatureUnit = it },
                    onWindSpeedUnitSelected = { windSpeedUnit = it },
                    onAppearanceModeSelected = { appearanceMode = it },
                )
            }
        }

        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithText("Temperature").assertIsDisplayed()
        composeRule.onNodeWithText("Wind Speed").assertIsDisplayed()
        composeRule.onNodeWithText("Appearance").assertIsDisplayed()

        composeRule.onNodeWithTag("settings_choice_Fahrenheit").performClick()
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_choice_m/s"))
        composeRule.onNodeWithTag("settings_choice_m/s").performClick()
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_choice_Dark"))
        composeRule.onNodeWithTag("settings_choice_Dark").performClick()

        assertEquals(TemperatureUnit.Fahrenheit, temperatureUnit)
        assertEquals(WindSpeedUnit.MetersPerSecond, windSpeedUnit)
        assertEquals(AppAppearanceMode.Dark, appearanceMode)
    }
}

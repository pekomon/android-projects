package com.pekomon.lockbox.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.pekomon.lockbox.ui.theme.LockBoxTheme
import org.junit.Rule
import org.junit.Test

class LockBoxAppTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun coldStartShowsLockedScreen() {
        composeRule.setContent {
            LockBoxTheme {
                LockBoxApp()
            }
        }

        composeRule.onNodeWithTag("lock_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Your vault is locked").assertIsDisplayed()
        composeRule.onNodeWithTag("unlock_button").assertIsDisplayed()
    }
}

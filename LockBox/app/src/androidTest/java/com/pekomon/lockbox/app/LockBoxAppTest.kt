package com.pekomon.lockbox.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pekomon.lockbox.core.security.AuthenticationResult
import com.pekomon.lockbox.core.security.BiometricAuthenticator
import com.pekomon.lockbox.core.security.BiometricAvailabilityReader
import com.pekomon.lockbox.core.security.InMemoryLockSession
import com.pekomon.lockbox.domain.model.BiometricAvailability
import com.pekomon.lockbox.ui.theme.LockBoxTheme
import kotlinx.coroutines.CompletableDeferred
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

    @Test
    fun unavailableBiometricsShowUnavailableState() {
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                biometricAvailabilityReader = StaticAvailabilityReader(BiometricAvailability.NoneEnrolled),
            ),
        )

        composeRule.onNodeWithTag("lock_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("unavailable_state").assertIsDisplayed()
        composeRule.onNodeWithText("No device credential is enrolled. Add a screen lock in Android settings to unlock LockBox.")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("unlock_button").assertIsNotEnabled()
    }

    @Test
    fun authenticatingStateIsShownWhilePromptIsOpen() {
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                biometricAuthenticator = SuspendedAuthenticator(),
            ),
        )

        composeRule.onNodeWithTag("unlock_button").performClick()

        composeRule.onNodeWithTag("authenticating_state").assertIsDisplayed()
        composeRule.onNodeWithText("Authenticating").assertIsDisplayed()
    }

    @Test
    fun canceledPromptShowsCanceledState() {
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                biometricAuthenticator = ResultAuthenticator(AuthenticationResult.Canceled),
            ),
        )

        composeRule.onNodeWithTag("unlock_button").performClick()

        composeRule.onNodeWithTag("auth_error_state").assertIsDisplayed()
        composeRule.onNodeWithText("Unlock canceled.").assertIsDisplayed()
    }

    @Test
    fun hardErrorShowsErrorState() {
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                biometricAuthenticator = ResultAuthenticator(AuthenticationResult.Error("Try again later")),
            ),
        )

        composeRule.onNodeWithTag("unlock_button").performClick()

        composeRule.onNodeWithTag("auth_error_state").assertIsDisplayed()
        composeRule.onNodeWithText("Try again later").assertIsDisplayed()
    }

    @Test
    fun successfulUnlockShowsEmptyVaultState() {
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                biometricAuthenticator = ResultAuthenticator(AuthenticationResult.Success),
            ),
        )

        composeRule.onNodeWithTag("unlock_button").performClick()

        composeRule.onNodeWithTag("unlocked_empty_state").assertIsDisplayed()
        composeRule.onNodeWithText("Vault is empty").assertIsDisplayed()
    }

    @Test
    fun alreadyUnlockedSessionShowsEmptyVaultState() {
        val lockSession = InMemoryLockSession().apply { unlock() }
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                lockSession = lockSession,
            ),
        )

        composeRule.onNodeWithTag("unlocked_empty_state").assertIsDisplayed()
        composeRule.onNodeWithText("Vault is empty").assertIsDisplayed()
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setLockBoxContent(
        appContainer: LockBoxAppContainer,
    ) {
        setContent {
            LockBoxTheme {
                LockBoxApp(appContainer)
            }
        }
    }

    private class StaticAvailabilityReader(
        private val availability: BiometricAvailability,
    ) : BiometricAvailabilityReader {
        override fun readAvailability(): BiometricAvailability = availability
    }

    private class ResultAuthenticator(
        private val result: AuthenticationResult,
    ) : BiometricAuthenticator {
        override suspend fun authenticate(): AuthenticationResult = result
    }

    private class SuspendedAuthenticator : BiometricAuthenticator {
        private val neverCompletes = CompletableDeferred<AuthenticationResult>()

        override suspend fun authenticate(): AuthenticationResult = neverCompletes.await()
    }
}

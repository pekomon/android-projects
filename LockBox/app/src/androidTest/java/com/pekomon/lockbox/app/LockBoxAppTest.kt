package com.pekomon.lockbox.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.pekomon.lockbox.core.security.AuthenticationResult
import com.pekomon.lockbox.core.security.BiometricAuthenticator
import com.pekomon.lockbox.core.security.BiometricAvailabilityReader
import com.pekomon.lockbox.core.security.InMemoryLockSession
import com.pekomon.lockbox.data.repository.InMemoryVaultRepository
import com.pekomon.lockbox.domain.model.BiometricAvailability
import com.pekomon.lockbox.domain.model.EntryKind
import com.pekomon.lockbox.domain.model.SecretPayload
import com.pekomon.lockbox.domain.model.VaultEntry
import com.pekomon.lockbox.domain.model.VaultEntryId
import com.pekomon.lockbox.domain.model.VaultEntryMetadata
import com.pekomon.lockbox.ui.theme.LockBoxTheme
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
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
    fun successfulUnlockShowsVaultEmptyState() {
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                biometricAuthenticator = ResultAuthenticator(AuthenticationResult.Success),
            ),
        )

        composeRule.onNodeWithTag("unlock_button").performClick()

        composeRule.onNodeWithTag("vault_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("vault_empty_state").assertIsDisplayed()
        composeRule.onNodeWithText("Vault is empty").assertIsDisplayed()
    }

    @Test
    fun alreadyUnlockedSessionShowsVaultEmptyState() {
        val lockSession = InMemoryLockSession().apply { unlock() }
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                lockSession = lockSession,
            ),
        )

        composeRule.onNodeWithTag("vault_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("vault_empty_state").assertIsDisplayed()
        composeRule.onNodeWithText("Vault is empty").assertIsDisplayed()
    }

    @Test
    fun settingsShowsSecurityAndStorageSummary() {
        val lockSession = InMemoryLockSession().apply { unlock() }
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                lockSession = lockSession,
            ),
        )

        composeRule.onNodeWithTag("settings_button").performClick()

        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithText("Unlock").assertIsDisplayed()
        composeRule.onNodeWithText("Storage").assertIsDisplayed()
        composeRule.onNodeWithText("Encryption").assertIsDisplayed()
        composeRule.onNodeWithText("Relock").assertIsDisplayed()
        composeRule.onNodeWithText("Backup").assertIsDisplayed()
        composeRule.onNodeWithText("The key is not authentication-bound in V1", substring = true)
            .assertIsDisplayed()

        composeRule.onNodeWithTag("settings_back_button").performClick()

        composeRule.onNodeWithTag("vault_screen").assertIsDisplayed()
    }

    @Test
    fun unlockedVaultListShowsMetadataOnly() {
        val lockSession = InMemoryLockSession().apply { unlock() }
        val vaultRepository = InMemoryVaultRepository()
        runBlocking {
            vaultRepository.saveEntry(sampleLoginEntry())
        }

        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                lockSession = lockSession,
                vaultRepository = vaultRepository,
            ),
        )

        composeRule.onNodeWithTag("vault_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("vault_entry_list").assertIsDisplayed()
        composeRule.onNodeWithTag("vault_entry_row_personal-email").assertIsDisplayed()
        composeRule.onNodeWithText("Personal email").assertIsDisplayed()
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.onNodeWithText("Secret hidden").assertIsDisplayed()
        composeRule.onNodeWithText("ada@example.com").assertDoesNotExist()
        composeRule.onNodeWithText("correct horse battery staple").assertDoesNotExist()
        composeRule.onNodeWithText("https://mail.example.test").assertDoesNotExist()
    }

    @Test
    fun tappingVaultRowShowsEntryDetail() {
        val lockSession = InMemoryLockSession().apply { unlock() }
        val vaultRepository = InMemoryVaultRepository()
        runBlocking {
            vaultRepository.saveEntry(sampleLoginEntry())
        }

        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                lockSession = lockSession,
                vaultRepository = vaultRepository,
            ),
        )

        composeRule.onNodeWithText("correct horse battery staple").assertDoesNotExist()
        composeRule.onNodeWithTag("vault_entry_row_personal-email").performClick()

        composeRule.onNodeWithTag("entry_detail_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Personal email").assertIsDisplayed()
        composeRule.onNodeWithText("ada@example.com").assertIsDisplayed()
        composeRule.onNodeWithText("correct horse battery staple").assertIsDisplayed()
        composeRule.onNodeWithText("https://mail.example.test").assertIsDisplayed()
    }

    @Test
    fun editorShowsFieldSpecificValidationErrors() {
        val lockSession = InMemoryLockSession().apply { unlock() }
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(lockSession = lockSession),
        )

        composeRule.onNodeWithTag("add_entry_button").performClick()
        composeRule.onNodeWithTag("save_entry_button").performScrollTo().performClick()

        composeRule.onNodeWithTag("entry_editor_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Title is required.").assertIsDisplayed()
        composeRule.onNodeWithText("Username is required.").assertIsDisplayed()
        composeRule.onNodeWithText("Password is required.").assertIsDisplayed()
    }

    @Test
    fun editorCreatesSecureNoteAndReturnsToRedactedList() {
        val lockSession = InMemoryLockSession().apply { unlock() }
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(lockSession = lockSession),
        )

        composeRule.onNodeWithTag("add_entry_button").performClick()
        composeRule.onNodeWithTag("kind_SecureNote").performClick()
        composeRule.onNodeWithTag("editor_title").performTextInput("Passport")
        composeRule.onNodeWithTag("editor_note_body").performTextInput("Number 123456789")
        composeRule.onNodeWithTag("save_entry_button").performScrollTo().performClick()

        composeRule.onNodeWithTag("vault_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Passport").assertIsDisplayed()
        composeRule.onNodeWithText("Secure note").assertIsDisplayed()
        composeRule.onNodeWithText("Secret hidden").assertIsDisplayed()
        composeRule.onNodeWithText("Number 123456789").assertDoesNotExist()
    }

    @Test
    fun editorUpdatesExistingEntryAndReturnsToList() {
        val lockSession = InMemoryLockSession().apply { unlock() }
        val vaultRepository = InMemoryVaultRepository()
        runBlocking {
            vaultRepository.saveEntry(sampleLoginEntry())
        }
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                lockSession = lockSession,
                vaultRepository = vaultRepository,
            ),
        )

        composeRule.onNodeWithTag("vault_entry_row_personal-email").performClick()
        composeRule.onNodeWithTag("edit_entry_button").performClick()
        composeRule.onNodeWithTag("entry_editor_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Edit entry").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_title").performTextClearance()
        composeRule.onNodeWithTag("editor_title").performTextInput("Work email")
        composeRule.onNodeWithTag("save_entry_button").performScrollTo().performClick()

        composeRule.onNodeWithTag("vault_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Work email").assertIsDisplayed()
        composeRule.onNodeWithText("Personal email").assertDoesNotExist()
        composeRule.onNodeWithText("correct horse battery staple").assertDoesNotExist()
    }

    @Test
    fun detailDeleteConfirmationRemovesEntryAndReturnsToList() {
        val lockSession = InMemoryLockSession().apply { unlock() }
        val vaultRepository = InMemoryVaultRepository()
        runBlocking {
            vaultRepository.saveEntry(sampleLoginEntry())
        }
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                lockSession = lockSession,
                vaultRepository = vaultRepository,
            ),
        )

        composeRule.onNodeWithTag("vault_entry_row_personal-email").performClick()
        composeRule.onNodeWithTag("delete_entry_button").performClick()
        composeRule.onNodeWithTag("delete_confirmation_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("confirm_delete_button").performClick()

        composeRule.onNodeWithTag("vault_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("vault_empty_state").assertIsDisplayed()
        composeRule.onNodeWithText("Personal email").assertDoesNotExist()
        composeRule.onNodeWithText("correct horse battery staple").assertDoesNotExist()
    }

    @Test
    fun relockFromDetailReturnsToLockAndReunlockShowsVaultList() {
        val lockSession = InMemoryLockSession().apply { unlock() }
        val vaultRepository = InMemoryVaultRepository()
        runBlocking {
            vaultRepository.saveEntry(sampleLoginEntry())
        }
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                lockSession = lockSession,
                vaultRepository = vaultRepository,
                biometricAuthenticator = ResultAuthenticator(AuthenticationResult.Success),
            ),
        )

        composeRule.onNodeWithTag("vault_entry_row_personal-email").performClick()
        composeRule.onNodeWithText("correct horse battery staple").assertIsDisplayed()

        lockSession.lock()

        composeRule.onNodeWithTag("lock_screen").assertIsDisplayed()
        composeRule.onNodeWithText("correct horse battery staple").assertDoesNotExist()
        composeRule.onNodeWithTag("unlock_button").performClick()
        composeRule.onNodeWithTag("vault_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("vault_entry_row_personal-email").assertIsDisplayed()
        composeRule.onNodeWithText("correct horse battery staple").assertDoesNotExist()
    }

    @Test
    fun relockFromEditorReturnsToLockAndClearsUnsavedInput() {
        val lockSession = InMemoryLockSession().apply { unlock() }
        composeRule.setLockBoxContent(
            appContainer = LockBoxAppContainer.fake(
                lockSession = lockSession,
                biometricAuthenticator = ResultAuthenticator(AuthenticationResult.Success),
            ),
        )

        composeRule.onNodeWithTag("add_entry_button").performClick()
        composeRule.onNodeWithTag("kind_SecureNote").performClick()
        composeRule.onNodeWithTag("editor_title").performTextInput("Temporary note")
        composeRule.onNodeWithTag("editor_note_body").performTextInput("Discard me")

        lockSession.lock()

        composeRule.onNodeWithTag("lock_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Temporary note").assertDoesNotExist()
        composeRule.onNodeWithText("Discard me").assertDoesNotExist()
        composeRule.onNodeWithTag("unlock_button").performClick()
        composeRule.onNodeWithTag("vault_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("vault_empty_state").assertIsDisplayed()
        composeRule.onNodeWithText("Temporary note").assertDoesNotExist()
        composeRule.onNodeWithText("Discard me").assertDoesNotExist()
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

    private fun sampleLoginEntry(): VaultEntry = VaultEntry(
        metadata = VaultEntryMetadata(
            id = VaultEntryId("personal-email"),
            title = "Personal email",
            kind = EntryKind.Login,
            createdAt = Instant.parse("2026-08-04T09:00:00Z"),
            updatedAt = Instant.parse("2026-08-04T10:00:00Z"),
        ),
        payload = SecretPayload.Login(
            username = "ada@example.com",
            password = "correct horse battery staple",
            url = "https://mail.example.test",
            notes = "Recovery codes in safe",
        ),
    )
}

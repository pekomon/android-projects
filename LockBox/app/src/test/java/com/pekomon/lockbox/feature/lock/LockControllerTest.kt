package com.pekomon.lockbox.feature.lock

import com.pekomon.lockbox.core.security.AuthenticationResult
import com.pekomon.lockbox.core.security.BiometricAuthenticator
import com.pekomon.lockbox.core.security.BiometricAvailabilityReader
import com.pekomon.lockbox.core.security.InMemoryLockSession
import com.pekomon.lockbox.domain.model.BiometricAvailability
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LockControllerTest {
    @Test
    fun successfulAuthenticationUnlocksExactlyOnce() = runTest {
        val lockSession = CountingLockSession()
        val authenticator = QueuedAuthenticator(AuthenticationResult.Success)
        val controller = newController(
            lockSession = lockSession,
            authenticator = authenticator,
        )

        controller.requestUnlock()
        advanceUntilIdle()

        assertTrue(controller.uiState.value.isUnlocked)
        assertEquals(1, lockSession.unlockCount)
        assertEquals(1, authenticator.callCount)
        assertEquals(PromptState.Idle, controller.uiState.value.promptState)
    }

    @Test
    fun failedAttemptDoesNotUnlockSession() = runTest {
        val controller = newController(
            authenticator = QueuedAuthenticator(AuthenticationResult.FailedAttempt),
        )

        controller.requestUnlock()
        advanceUntilIdle()

        assertFalse(controller.uiState.value.isUnlocked)
        assertEquals(PromptState.FailedAttempt, controller.uiState.value.promptState)
    }

    @Test
    fun canceledPromptDoesNotUnlockSession() = runTest {
        val controller = newController(
            authenticator = QueuedAuthenticator(AuthenticationResult.Canceled),
        )

        controller.requestUnlock()
        advanceUntilIdle()

        assertFalse(controller.uiState.value.isUnlocked)
        assertEquals(PromptState.Canceled, controller.uiState.value.promptState)
    }

    @Test
    fun hardErrorDoesNotUnlockSession() = runTest {
        val controller = newController(
            authenticator = QueuedAuthenticator(AuthenticationResult.Error("Sensor unavailable")),
        )

        controller.requestUnlock()
        advanceUntilIdle()

        assertFalse(controller.uiState.value.isUnlocked)
        assertEquals(
            PromptState.Error("Sensor unavailable"),
            controller.uiState.value.promptState,
        )
    }

    @Test
    fun unavailableBiometricsDoNotLaunchPrompt() = runTest {
        val authenticator = QueuedAuthenticator(AuthenticationResult.Success)
        val controller = newController(
            availability = BiometricAvailability.NoneEnrolled,
            authenticator = authenticator,
        )

        controller.requestUnlock()
        advanceUntilIdle()

        assertFalse(controller.uiState.value.isUnlocked)
        assertEquals(0, authenticator.callCount)
    }

    @Test
    fun duplicateUnlockRequestsDoNotLaunchDuplicatePrompts() = runTest {
        val authenticator = SuspendingAuthenticator()
        val controller = newController(authenticator = authenticator)

        controller.requestUnlock()
        runCurrent()
        controller.requestUnlock()
        runCurrent()

        assertEquals(1, authenticator.callCount)
        assertEquals(PromptState.Authenticating, controller.uiState.value.promptState)

        authenticator.result = AuthenticationResult.Success
        advanceUntilIdle()

        assertTrue(controller.uiState.value.isUnlocked)
        assertEquals(1, authenticator.callCount)
    }

    @Test
    fun lockClearsPromptStateAndLocksSession() = runTest {
        val lockSession = InMemoryLockSession()
        val controller = newController(
            lockSession = lockSession,
            authenticator = QueuedAuthenticator(AuthenticationResult.Success),
        )
        controller.requestUnlock()
        advanceUntilIdle()

        controller.lock()
        advanceUntilIdle()

        assertFalse(controller.uiState.value.isUnlocked)
        assertEquals(PromptState.Idle, controller.uiState.value.promptState)
    }

    private fun TestScope.newController(
        lockSession: InMemoryLockSession = InMemoryLockSession(),
        availability: BiometricAvailability = BiometricAvailability.Available,
        authenticator: BiometricAuthenticator = QueuedAuthenticator(AuthenticationResult.Success),
    ): LockController = LockController(
        lockSession = lockSession,
        availabilityReader = StaticAvailabilityReader(availability),
        authenticator = authenticator,
        scope = TestScope(StandardTestDispatcher(testScheduler)),
    )

    private class StaticAvailabilityReader(
        private val availability: BiometricAvailability,
    ) : BiometricAvailabilityReader {
        override fun readAvailability(): BiometricAvailability = availability
    }

    private class QueuedAuthenticator(
        private val result: AuthenticationResult,
    ) : BiometricAuthenticator {
        var callCount = 0
            private set

        override suspend fun authenticate(): AuthenticationResult {
            callCount += 1
            return result
        }
    }

    private class SuspendingAuthenticator : BiometricAuthenticator {
        var callCount = 0
            private set
        var result: AuthenticationResult = AuthenticationResult.Canceled

        override suspend fun authenticate(): AuthenticationResult {
            callCount += 1
            delay(1_000)
            return result
        }
    }

    private class CountingLockSession : InMemoryLockSession() {
        var unlockCount = 0
            private set

        override fun unlock() {
            unlockCount += 1
            super.unlock()
        }
    }
}

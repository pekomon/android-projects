package com.pekomon.lockbox.feature.lock

import com.pekomon.lockbox.core.security.AuthenticationResult
import com.pekomon.lockbox.core.security.BiometricAuthenticator
import com.pekomon.lockbox.core.security.BiometricAvailabilityReader
import com.pekomon.lockbox.core.security.LockSession
import com.pekomon.lockbox.domain.model.BiometricAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LockController(
    private val lockSession: LockSession,
    private val availabilityReader: BiometricAvailabilityReader,
    private val authenticator: BiometricAuthenticator,
    private val scope: CoroutineScope,
) {
    private val availability = MutableStateFlow(availabilityReader.readAvailability())
    private val promptState = MutableStateFlow<PromptState>(PromptState.Idle)
    private var authenticationJob: Job? = null

    val uiState: StateFlow<LockUiState> = combine(
        lockSession.isUnlocked,
        availability,
        promptState,
    ) { isUnlocked, availability, promptState ->
        LockUiState(
            isUnlocked = isUnlocked,
            availability = availability,
            promptState = promptState,
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = LockUiState(
            isUnlocked = lockSession.isUnlocked.value,
            availability = availability.value,
        ),
    )

    fun refreshAvailability() {
        availability.value = availabilityReader.readAvailability()
    }

    fun requestUnlock() {
        refreshAvailability()
        if (lockSession.isUnlocked.value || availability.value != BiometricAvailability.Available) {
            return
        }
        if (authenticationJob?.isActive == true) {
            return
        }

        authenticationJob = scope.launch {
            promptState.value = PromptState.Authenticating
            when (val result = authenticator.authenticate()) {
                AuthenticationResult.Success -> {
                    lockSession.unlock()
                    promptState.value = PromptState.Idle
                }

                AuthenticationResult.FailedAttempt -> {
                    promptState.value = PromptState.FailedAttempt
                }

                AuthenticationResult.Canceled -> {
                    promptState.value = PromptState.Canceled
                }

                is AuthenticationResult.Error -> {
                    promptState.value = PromptState.Error(result.message)
                }
            }
        }
    }

    fun lock() {
        authenticationJob?.cancel()
        promptState.value = PromptState.Idle
        lockSession.lock()
    }
}

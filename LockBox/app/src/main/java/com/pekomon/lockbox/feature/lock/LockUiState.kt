package com.pekomon.lockbox.feature.lock

import com.pekomon.lockbox.domain.model.BiometricAvailability

data class LockUiState(
    val isUnlocked: Boolean,
    val availability: BiometricAvailability,
    val promptState: PromptState = PromptState.Idle,
) {
    val canRequestUnlock: Boolean
        get() = !isUnlocked &&
            availability == BiometricAvailability.Available &&
            promptState != PromptState.Authenticating
}

sealed interface PromptState {
    data object Idle : PromptState
    data object Authenticating : PromptState
    data object FailedAttempt : PromptState
    data object Canceled : PromptState

    data class Error(
        val message: String,
    ) : PromptState
}

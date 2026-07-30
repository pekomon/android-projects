package com.pekomon.lockbox.domain.model

sealed interface LockState {
    data object Locked : LockState
    data object Authenticating : LockState
    data object Unlocked : LockState

    data class Unavailable(
        val availability: BiometricAvailability,
    ) : LockState

    data class Error(
        val message: String,
    ) : LockState
}

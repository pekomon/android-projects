package com.pekomon.lockbox.core.security

sealed interface AuthenticationResult {
    data object Success : AuthenticationResult
    data object FailedAttempt : AuthenticationResult
    data object Canceled : AuthenticationResult

    data class Error(
        val message: String,
    ) : AuthenticationResult
}

interface BiometricAuthenticator {
    suspend fun authenticate(): AuthenticationResult
}

class FakeBiometricAuthenticator : BiometricAuthenticator {
    override suspend fun authenticate(): AuthenticationResult = AuthenticationResult.Success
}

package com.pekomon.lockbox.core.security

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidBiometricAuthenticator(
    private val activity: FragmentActivity,
) : BiometricAuthenticator {
    override suspend fun authenticate(): AuthenticationResult = suspendCancellableCoroutine { continuation ->
        var completed = false

        fun complete(result: AuthenticationResult) {
            if (!completed && continuation.isActive) {
                completed = true
                continuation.resume(result)
            }
        }

        lateinit var prompt: BiometricPrompt
        prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    complete(AuthenticationResult.Success)
                }

                override fun onAuthenticationFailed() {
                    complete(AuthenticationResult.FailedAttempt)
                    prompt.cancelAuthentication()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val result = when (errorCode) {
                        BiometricPrompt.ERROR_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED,
                        -> AuthenticationResult.Canceled

                        else -> AuthenticationResult.Error(errString.toString())
                    }
                    complete(result)
                }
            },
        )

        continuation.invokeOnCancellation {
            prompt.cancelAuthentication()
        }

        prompt.authenticate(PROMPT_INFO)
    }

    companion object {
        private val PROMPT_INFO = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock LockBox")
            .setSubtitle("Authenticate to open your vault")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
    }
}

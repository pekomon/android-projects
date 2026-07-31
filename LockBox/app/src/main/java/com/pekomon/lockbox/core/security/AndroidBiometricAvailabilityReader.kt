package com.pekomon.lockbox.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import com.pekomon.lockbox.domain.model.BiometricAvailability

private const val LOCKBOX_AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

class AndroidBiometricAvailabilityReader(
    context: Context,
) : BiometricAvailabilityReader {
    private val biometricManager = BiometricManager.from(context)

    override fun readAvailability(): BiometricAvailability =
        biometricManager.canAuthenticate(LOCKBOX_AUTHENTICATORS).toBiometricAvailability()
}

fun Int.toBiometricAvailability(): BiometricAvailability = when (this) {
    BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NoneEnrolled
    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
    BiometricManager.BIOMETRIC_STATUS_UNKNOWN,
    -> BiometricAvailability.TemporarilyUnavailable

    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
    BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED,
    BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
    -> BiometricAvailability.Unsupported

    else -> BiometricAvailability.TemporarilyUnavailable
}

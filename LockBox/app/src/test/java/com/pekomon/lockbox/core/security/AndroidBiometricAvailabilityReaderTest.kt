package com.pekomon.lockbox.core.security

import androidx.biometric.BiometricManager
import com.pekomon.lockbox.domain.model.BiometricAvailability
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidBiometricAvailabilityReaderTest {
    @Test
    fun biometricSuccessMapsToAvailable() {
        assertEquals(
            BiometricAvailability.Available,
            BiometricManager.BIOMETRIC_SUCCESS.toBiometricAvailability(),
        )
    }

    @Test
    fun noneEnrolledMapsToNoneEnrolled() {
        assertEquals(
            BiometricAvailability.NoneEnrolled,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED.toBiometricAvailability(),
        )
    }

    @Test
    fun transientFailuresMapToTemporarilyUnavailable() {
        assertEquals(
            BiometricAvailability.TemporarilyUnavailable,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE.toBiometricAvailability(),
        )
        assertEquals(
            BiometricAvailability.TemporarilyUnavailable,
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN.toBiometricAvailability(),
        )
    }

    @Test
    fun unsupportedHardwareMapsToUnsupported() {
        assertEquals(
            BiometricAvailability.Unsupported,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE.toBiometricAvailability(),
        )
        assertEquals(
            BiometricAvailability.Unsupported,
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED.toBiometricAvailability(),
        )
    }
}

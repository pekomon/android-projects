package com.pekomon.lockbox.domain.model

sealed interface BiometricAvailability {
    data object Available : BiometricAvailability
    data object NoneEnrolled : BiometricAvailability
    data object TemporarilyUnavailable : BiometricAvailability
    data object Unsupported : BiometricAvailability
}

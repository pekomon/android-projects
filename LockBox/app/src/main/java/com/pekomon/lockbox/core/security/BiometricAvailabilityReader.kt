package com.pekomon.lockbox.core.security

import com.pekomon.lockbox.domain.model.BiometricAvailability

interface BiometricAvailabilityReader {
    fun readAvailability(): BiometricAvailability
}

class FakeBiometricAvailabilityReader : BiometricAvailabilityReader {
    override fun readAvailability(): BiometricAvailability = BiometricAvailability.Available
}

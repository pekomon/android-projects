package com.pekomon.lockbox.app

import android.content.Context
import com.pekomon.lockbox.core.crypto.CryptoService
import com.pekomon.lockbox.core.crypto.NoOpCryptoService
import com.pekomon.lockbox.core.security.AndroidBiometricAvailabilityReader
import com.pekomon.lockbox.core.security.BiometricAvailabilityReader
import com.pekomon.lockbox.core.security.BiometricAuthenticator
import com.pekomon.lockbox.core.security.FakeBiometricAvailabilityReader
import com.pekomon.lockbox.core.security.FakeBiometricAuthenticator
import com.pekomon.lockbox.core.security.InMemoryLockSession
import com.pekomon.lockbox.core.security.LockSession
import com.pekomon.lockbox.data.repository.InMemoryVaultRepository
import com.pekomon.lockbox.domain.repository.VaultRepository

class LockBoxAppContainer private constructor(
    val lockSession: LockSession,
    val biometricAvailabilityReader: BiometricAvailabilityReader,
    val biometricAuthenticator: BiometricAuthenticator,
    val cryptoService: CryptoService,
    val vaultRepository: VaultRepository,
) {
    constructor(context: Context) : this(
        lockSession = InMemoryLockSession(),
        biometricAvailabilityReader = AndroidBiometricAvailabilityReader(context),
        biometricAuthenticator = FakeBiometricAuthenticator(),
        cryptoService = NoOpCryptoService(),
        vaultRepository = InMemoryVaultRepository(),
    )

    companion object {
        fun preview(): LockBoxAppContainer = LockBoxAppContainer(
            lockSession = InMemoryLockSession(),
            biometricAvailabilityReader = FakeBiometricAvailabilityReader(),
            biometricAuthenticator = FakeBiometricAuthenticator(),
            cryptoService = NoOpCryptoService(),
            vaultRepository = InMemoryVaultRepository(),
        )
    }
}

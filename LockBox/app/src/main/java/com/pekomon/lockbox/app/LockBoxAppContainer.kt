package com.pekomon.lockbox.app

import android.content.Context
import com.pekomon.lockbox.core.crypto.CryptoService
import com.pekomon.lockbox.core.crypto.NoOpCryptoService
import com.pekomon.lockbox.core.security.BiometricAvailabilityReader
import com.pekomon.lockbox.core.security.BiometricAuthenticator
import com.pekomon.lockbox.core.security.FakeBiometricAvailabilityReader
import com.pekomon.lockbox.core.security.FakeBiometricAuthenticator
import com.pekomon.lockbox.core.security.InMemoryLockSession
import com.pekomon.lockbox.core.security.LockSession
import com.pekomon.lockbox.data.repository.InMemoryVaultRepository
import com.pekomon.lockbox.domain.repository.VaultRepository

class LockBoxAppContainer(
    @Suppress("UNUSED_PARAMETER") context: Context,
) {
    val lockSession: LockSession = InMemoryLockSession()
    val biometricAvailabilityReader: BiometricAvailabilityReader = FakeBiometricAvailabilityReader()
    val biometricAuthenticator: BiometricAuthenticator = FakeBiometricAuthenticator()
    val cryptoService: CryptoService = NoOpCryptoService()
    val vaultRepository: VaultRepository = InMemoryVaultRepository()

    companion object {
        fun preview(): LockBoxAppContainer = LockBoxAppContainer(
            context = android.app.Application(),
        )
    }
}

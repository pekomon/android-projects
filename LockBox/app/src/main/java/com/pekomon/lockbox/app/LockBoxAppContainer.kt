package com.pekomon.lockbox.app

import android.content.Context
import androidx.room.Room
import com.pekomon.lockbox.core.crypto.AndroidKeystoreCryptoService
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
import com.pekomon.lockbox.data.local.VaultDatabase
import com.pekomon.lockbox.data.repository.PersistentVaultRepository
import com.pekomon.lockbox.domain.repository.VaultRepository

private const val VAULT_DATABASE_NAME = "lockbox_vault.db"

class LockBoxAppContainer private constructor(
    val lockSession: LockSession,
    val biometricAvailabilityReader: BiometricAvailabilityReader,
    val biometricAuthenticator: BiometricAuthenticator,
    val cryptoService: CryptoService,
    val vaultRepository: VaultRepository,
) {
    constructor(context: Context) : this(
        context = context,
        cryptoService = AndroidKeystoreCryptoService(),
    )

    private constructor(
        context: Context,
        cryptoService: CryptoService,
    ) : this(
        lockSession = InMemoryLockSession(),
        biometricAvailabilityReader = AndroidBiometricAvailabilityReader(context),
        biometricAuthenticator = FakeBiometricAuthenticator(),
        cryptoService = cryptoService,
        vaultRepository = persistentVaultRepository(
            context = context,
            cryptoService = cryptoService,
        ),
    )

    companion object {
        fun fake(
            lockSession: LockSession = InMemoryLockSession(),
            biometricAvailabilityReader: BiometricAvailabilityReader = FakeBiometricAvailabilityReader(),
            biometricAuthenticator: BiometricAuthenticator = FakeBiometricAuthenticator(),
            cryptoService: CryptoService = NoOpCryptoService(),
            vaultRepository: VaultRepository = InMemoryVaultRepository(),
        ): LockBoxAppContainer = LockBoxAppContainer(
            lockSession = lockSession,
            biometricAvailabilityReader = biometricAvailabilityReader,
            biometricAuthenticator = biometricAuthenticator,
            cryptoService = cryptoService,
            vaultRepository = vaultRepository,
        )

        fun preview(): LockBoxAppContainer = LockBoxAppContainer(
            lockSession = InMemoryLockSession(),
            biometricAvailabilityReader = FakeBiometricAvailabilityReader(),
            biometricAuthenticator = FakeBiometricAuthenticator(),
            cryptoService = NoOpCryptoService(),
            vaultRepository = InMemoryVaultRepository(),
        )

        private fun persistentVaultRepository(
            context: Context,
            cryptoService: CryptoService,
        ): PersistentVaultRepository {
            val database = Room.databaseBuilder(
                context.applicationContext,
                VaultDatabase::class.java,
                VAULT_DATABASE_NAME,
            ).build()
            return PersistentVaultRepository(
                dao = database.vaultEntryDao(),
                cryptoService = cryptoService,
            )
        }
    }
}

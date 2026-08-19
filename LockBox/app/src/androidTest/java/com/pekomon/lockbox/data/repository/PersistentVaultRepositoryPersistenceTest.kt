package com.pekomon.lockbox.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pekomon.lockbox.core.crypto.AndroidKeystoreCryptoService
import com.pekomon.lockbox.data.local.VaultDatabase
import com.pekomon.lockbox.domain.model.EntryKind
import com.pekomon.lockbox.domain.model.SecretPayload
import com.pekomon.lockbox.domain.model.VaultEntry
import com.pekomon.lockbox.domain.model.VaultEntryId
import com.pekomon.lockbox.domain.model.VaultEntryMetadata
import java.security.KeyStore
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistentVaultRepositoryPersistenceTest {
    private val keyAliases = mutableListOf<String>()
    private val databases = mutableListOf<VaultDatabase>()

    @After
    fun tearDown() {
        databases.forEach { it.close() }
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        keyAliases.forEach { alias ->
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
        }
    }

    @Test
    fun repositoryPersistsEncryptedPayloadWithoutPlaintext() = runBlocking {
        val database = database()
        val cryptoService = cryptoService()
        val repository = PersistentVaultRepository(
            dao = database.vaultEntryDao(),
            cryptoService = cryptoService,
        )
        val entry = loginEntry()

        repository.saveEntry(entry)

        val storedEntry = database.vaultEntryDao().getStoredEntry(entry.metadata.id.value)
        requireNotNull(storedEntry)
        val ciphertextText = storedEntry.encryptedPayload.ciphertext.decodeToString()
        assertFalse(ciphertextText.contains("ada@example.com"))
        assertFalse(ciphertextText.contains("correct horse battery staple"))
        assertFalse(ciphertextText.contains("example.test"))

        val restartedRepository = PersistentVaultRepository(
            dao = database.vaultEntryDao(),
            cryptoService = cryptoService,
        )
        assertEquals(listOf(entry.metadata), restartedRepository.entries.first())
        assertEquals(entry, restartedRepository.getEntry(entry.metadata.id))
    }

    private fun database(): VaultDatabase {
        val database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            VaultDatabase::class.java,
        ).build()
        databases += database
        return database
    }

    private fun cryptoService(): AndroidKeystoreCryptoService {
        val alias = "lockbox_persistence_test_${UUID.randomUUID()}"
        keyAliases += alias
        return AndroidKeystoreCryptoService(keyAlias = alias)
    }

    private fun loginEntry(): VaultEntry = VaultEntry(
        metadata = VaultEntryMetadata(
            id = VaultEntryId("github"),
            title = "GitHub",
            kind = EntryKind.Login,
            createdAt = Instant.parse("2026-08-19T08:00:00Z"),
            updatedAt = Instant.parse("2026-08-19T09:00:00Z"),
        ),
        payload = SecretPayload.Login(
            username = "ada@example.com",
            password = "correct horse battery staple",
            url = "https://example.test",
            notes = "Recovery code elsewhere",
        ),
    )
}

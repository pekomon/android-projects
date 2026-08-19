package com.pekomon.lockbox.data.repository

import com.pekomon.lockbox.core.crypto.CryptoFailure
import com.pekomon.lockbox.core.crypto.CryptoOperationResult
import com.pekomon.lockbox.core.crypto.CryptoService
import com.pekomon.lockbox.core.crypto.DecryptSecretRequest
import com.pekomon.lockbox.core.crypto.EncryptSecretRequest
import com.pekomon.lockbox.core.crypto.EncryptedPayload
import com.pekomon.lockbox.core.crypto.VaultCryptoAssociatedData
import com.pekomon.lockbox.data.local.EncryptedSecretPayloadEntity
import com.pekomon.lockbox.data.local.StoredVaultEntry
import com.pekomon.lockbox.data.local.VaultEntryDao
import com.pekomon.lockbox.data.local.VaultEntryMetadataEntity
import com.pekomon.lockbox.domain.model.EntryKind
import com.pekomon.lockbox.domain.model.SecretPayload
import com.pekomon.lockbox.domain.model.VaultEntry
import com.pekomon.lockbox.domain.model.VaultEntryId
import com.pekomon.lockbox.domain.model.VaultEntryMetadata
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistentVaultRepositoryTest {
    @Test
    fun saveEntryStoresMetadataAndEncryptedPayload() = runTest {
        val dao = FakeVaultEntryDao()
        val cryptoService = RecordingCryptoService()
        val repository = PersistentVaultRepository(
            dao = dao,
            cryptoService = cryptoService,
        )
        val entry = loginEntry(id = "github", title = "GitHub")

        repository.saveEntry(entry)

        val storedEntry = dao.getStoredEntry("github")
        requireNotNull(storedEntry)
        assertEquals(entry.metadata, storedEntry.metadata.toDomain())
        assertFalse(storedEntry.encryptedPayload.ciphertext.decodeToString().contains("correct horse"))
        assertEquals(1, storedEntry.encryptedPayload.schemaVersion)
        assertArrayEquals(
            VaultCryptoAssociatedData.forPayload(entry.metadata.id, payloadVersion = 1),
            cryptoService.encryptRequests.single().associatedData,
        )
    }

    @Test
    fun savedEntryCanBeReadById() = runTest {
        val repository = PersistentVaultRepository(
            dao = FakeVaultEntryDao(),
            cryptoService = RecordingCryptoService(),
        )
        val entry = cardEntry(id = "travel-card", title = "Travel card")

        repository.saveEntry(entry)

        assertEquals(entry, repository.getEntry(entry.metadata.id))
    }

    @Test
    fun entriesEmitMetadataSortedByDaoOrder() = runTest {
        val repository = PersistentVaultRepository(
            dao = FakeVaultEntryDao(),
            cryptoService = RecordingCryptoService(),
        )
        val older = loginEntry(
            id = "older",
            title = "Older",
            updatedAt = Instant.parse("2026-08-10T08:00:00Z"),
        )
        val newer = noteEntry(
            id = "newer",
            title = "Newer",
            updatedAt = Instant.parse("2026-08-10T11:00:00Z"),
        )

        repository.saveEntry(older)
        repository.saveEntry(newer)

        assertEquals(
            listOf(newer.metadata, older.metadata),
            repository.entries.first(),
        )
    }

    @Test
    fun deleteEntryRemovesMetadataAndPayloadTogether() = runTest {
        val dao = FakeVaultEntryDao()
        val repository = PersistentVaultRepository(
            dao = dao,
            cryptoService = RecordingCryptoService(),
        )
        val entry = noteEntry(id = "note", title = "Private note")

        repository.saveEntry(entry)
        repository.deleteEntry(entry.metadata.id)

        assertNull(repository.getEntry(entry.metadata.id))
        assertEquals(emptyList<VaultEntryMetadata>(), repository.entries.first())
        assertFalse(dao.hasPayload(entry.metadata.id.value))
    }

    @Test
    fun getEntryFailsClosedWhenDecryptionFails() = runTest {
        val repository = PersistentVaultRepository(
            dao = FakeVaultEntryDao(),
            cryptoService = RecordingCryptoService(
                decryptFailure = CryptoFailure.AuthenticationFailed,
            ),
        )
        val entry = loginEntry(id = "email", title = "Email")

        repository.saveEntry(entry)

        val exception = runCatching { repository.getEntry(entry.metadata.id) }.exceptionOrNull()
        assertTrue(exception is VaultRepositoryException.DecryptionFailed)
    }

    @Test
    fun saveEntryFailsClosedWhenEncryptionFails() = runTest {
        val dao = FakeVaultEntryDao()
        val repository = PersistentVaultRepository(
            dao = dao,
            cryptoService = RecordingCryptoService(
                encryptFailure = CryptoFailure.EncryptionFailed,
            ),
        )
        val entry = loginEntry(id = "email", title = "Email")

        val exception = runCatching { repository.saveEntry(entry) }.exceptionOrNull()

        assertTrue(exception is VaultRepositoryException.EncryptionFailed)
        assertNull(dao.getStoredEntry(entry.metadata.id.value))
    }

    @Test
    fun getEntryFailsClosedWhenPayloadCannotBeDecoded() = runTest {
        val dao = FakeVaultEntryDao()
        val repository = PersistentVaultRepository(
            dao = dao,
            cryptoService = RecordingCryptoService(
                decryptOverride = ByteArray(4),
            ),
        )
        val entry = noteEntry(id = "note", title = "Private note")

        repository.saveEntry(entry)

        val exception = runCatching { repository.getEntry(entry.metadata.id) }.exceptionOrNull()
        assertTrue(exception is VaultRepositoryException.PayloadDecodingFailed)
    }

    private fun loginEntry(
        id: String,
        title: String,
        updatedAt: Instant = Instant.parse("2026-08-10T09:00:00Z"),
    ): VaultEntry = VaultEntry(
        metadata = metadata(
            id = id,
            title = title,
            kind = EntryKind.Login,
            updatedAt = updatedAt,
        ),
        payload = SecretPayload.Login(
            username = "ada@example.com",
            password = "correct horse battery staple",
            url = "https://example.test",
            notes = "Recovery code elsewhere",
        ),
    )

    private fun noteEntry(
        id: String,
        title: String,
        updatedAt: Instant = Instant.parse("2026-08-10T09:00:00Z"),
    ): VaultEntry = VaultEntry(
        metadata = metadata(
            id = id,
            title = title,
            kind = EntryKind.SecureNote,
            updatedAt = updatedAt,
        ),
        payload = SecretPayload.SecureNote(
            body = "Private note body",
        ),
    )

    private fun cardEntry(
        id: String,
        title: String,
    ): VaultEntry = VaultEntry(
        metadata = metadata(
            id = id,
            title = title,
            kind = EntryKind.Card,
            updatedAt = Instant.parse("2026-08-10T09:00:00Z"),
        ),
        payload = SecretPayload.Card(
            cardholder = "Ada Lovelace",
            number = "4111111111111111",
            expiry = "09/29",
            securityCode = "123",
            notes = "Travel only",
        ),
    )

    private fun metadata(
        id: String,
        title: String,
        kind: EntryKind,
        updatedAt: Instant,
    ): VaultEntryMetadata = VaultEntryMetadata(
        id = VaultEntryId(id),
        title = title,
        kind = kind,
        createdAt = Instant.parse("2026-08-10T07:00:00Z"),
        updatedAt = updatedAt,
    )
}

private class FakeVaultEntryDao : VaultEntryDao {
    private val metadataRows = linkedMapOf<String, VaultEntryMetadataEntity>()
    private val payloadRows = linkedMapOf<String, EncryptedSecretPayloadEntity>()
    private val metadataFlow = MutableStateFlow<List<VaultEntryMetadataEntity>>(emptyList())

    override fun observeMetadata(): Flow<List<VaultEntryMetadataEntity>> = metadataFlow

    override suspend fun getStoredEntry(entryId: String): StoredVaultEntry? {
        val metadata = metadataRows[entryId] ?: return null
        val payload = payloadRows[entryId] ?: return null
        return StoredVaultEntry(
            metadata = metadata,
            encryptedPayload = payload,
        )
    }

    override suspend fun saveStoredEntry(
        metadata: VaultEntryMetadataEntity,
        encryptedPayload: EncryptedSecretPayloadEntity,
    ) {
        upsertMetadata(metadata)
        upsertEncryptedPayload(encryptedPayload)
    }

    override suspend fun deleteStoredEntry(entryId: String) {
        metadataRows -= entryId
        payloadRows -= entryId
        emitMetadata()
    }

    override suspend fun upsertMetadata(metadata: VaultEntryMetadataEntity) {
        metadataRows[metadata.entryId] = metadata
        emitMetadata()
    }

    override suspend fun upsertEncryptedPayload(encryptedPayload: EncryptedSecretPayloadEntity) {
        require(metadataRows.containsKey(encryptedPayload.entryId))
        payloadRows[encryptedPayload.entryId] = encryptedPayload
    }

    fun hasPayload(entryId: String): Boolean = payloadRows.containsKey(entryId)

    private fun emitMetadata() {
        metadataFlow.value = metadataRows.values.sortedByDescending { it.updatedAt }
    }
}

private class RecordingCryptoService(
    private val encryptFailure: CryptoFailure? = null,
    private val decryptFailure: CryptoFailure? = null,
    private val decryptOverride: ByteArray? = null,
) : CryptoService {
    val encryptRequests = mutableListOf<EncryptSecretRequest>()

    override fun encrypt(
        request: EncryptSecretRequest,
    ): CryptoOperationResult<EncryptedPayload> {
        encryptRequests += request
        encryptFailure?.let { return CryptoOperationResult.Failure(it) }
        return CryptoOperationResult.Success(
            EncryptedPayload(
                iv = request.associatedData.copyOf(),
                ciphertext = request.plaintext.reversedArray(),
                schemaVersion = request.schemaVersion,
            ),
        )
    }

    override fun decrypt(
        request: DecryptSecretRequest,
    ): CryptoOperationResult<ByteArray> {
        decryptFailure?.let { return CryptoOperationResult.Failure(it) }
        return CryptoOperationResult.Success(
            decryptOverride ?: request.payload.ciphertext.reversedArray(),
        )
    }
}

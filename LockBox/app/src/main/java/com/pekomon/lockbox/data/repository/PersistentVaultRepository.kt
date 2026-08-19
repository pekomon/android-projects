package com.pekomon.lockbox.data.repository

import com.pekomon.lockbox.core.crypto.CryptoOperationResult
import com.pekomon.lockbox.core.crypto.CryptoService
import com.pekomon.lockbox.core.crypto.DecryptSecretRequest
import com.pekomon.lockbox.core.crypto.EncryptSecretRequest
import com.pekomon.lockbox.core.crypto.EncryptedPayload
import com.pekomon.lockbox.core.crypto.VaultCryptoAssociatedData
import com.pekomon.lockbox.data.local.EncryptedSecretPayloadEntity
import com.pekomon.lockbox.data.local.EncodedSecretPayload
import com.pekomon.lockbox.data.local.SecretPayloadCodec
import com.pekomon.lockbox.data.local.SecretPayloadCodecException
import com.pekomon.lockbox.data.local.StoredVaultEntry
import com.pekomon.lockbox.data.local.VaultEntryDao
import com.pekomon.lockbox.data.local.VaultEntryMetadataEntity
import com.pekomon.lockbox.domain.model.VaultEntry
import com.pekomon.lockbox.domain.model.VaultEntryId
import com.pekomon.lockbox.domain.model.VaultEntryMetadata
import com.pekomon.lockbox.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PersistentVaultRepository(
    private val dao: VaultEntryDao,
    private val cryptoService: CryptoService,
    private val payloadCodec: SecretPayloadCodec = SecretPayloadCodec(),
) : VaultRepository {
    override val entries: Flow<List<VaultEntryMetadata>> = dao.observeMetadata()
        .map { rows -> rows.map { it.toDomain() } }

    override suspend fun getEntry(id: VaultEntryId): VaultEntry? {
        val storedEntry = dao.getStoredEntry(id.value) ?: return null
        return storedEntry.toDomainEntry()
    }

    override suspend fun saveEntry(entry: VaultEntry) {
        val encodedPayload = payloadCodec.encode(entry.payload)
        val encryptedPayload = encrypt(
            entryId = entry.metadata.id,
            encodedPayload = encodedPayload,
        )

        dao.saveStoredEntry(
            metadata = VaultEntryMetadataEntity.fromDomain(entry.metadata),
            encryptedPayload = EncryptedSecretPayloadEntity(
                entryId = entry.metadata.id.value,
                iv = encryptedPayload.iv,
                ciphertext = encryptedPayload.ciphertext,
                schemaVersion = encryptedPayload.schemaVersion,
            ),
        )
    }

    override suspend fun deleteEntry(id: VaultEntryId) {
        dao.deleteStoredEntry(id.value)
    }

    private fun StoredVaultEntry.toDomainEntry(): VaultEntry {
        val metadata = metadata.toDomain()
        val decryptedPayload = decrypt(
            entryId = metadata.id,
            encryptedPayload = encryptedPayload,
        )

        val payload = try {
            payloadCodec.decode(
                EncodedSecretPayload(
                    schemaVersion = decryptedPayload.schemaVersion,
                    kind = metadata.kind,
                    bytes = decryptedPayload.plaintext,
                ),
            )
        } catch (exception: SecretPayloadCodecException) {
            throw VaultRepositoryException.PayloadDecodingFailed
        } catch (exception: IllegalArgumentException) {
            throw VaultRepositoryException.PayloadDecodingFailed
        }

        return VaultEntry(
            metadata = metadata,
            payload = payload,
        )
    }

    private fun encrypt(
        entryId: VaultEntryId,
        encodedPayload: EncodedSecretPayload,
    ): EncryptedPayload {
        val result = cryptoService.encrypt(
            EncryptSecretRequest(
                plaintext = encodedPayload.bytes,
                associatedData = VaultCryptoAssociatedData.forPayload(
                    entryId = entryId,
                    payloadVersion = encodedPayload.schemaVersion,
                ),
                schemaVersion = encodedPayload.schemaVersion,
            ),
        )

        return when (result) {
            is CryptoOperationResult.Success -> result.value
            is CryptoOperationResult.Failure -> throw VaultRepositoryException.EncryptionFailed
        }
    }

    private fun decrypt(
        entryId: VaultEntryId,
        encryptedPayload: EncryptedSecretPayloadEntity,
    ): DecryptedStoredPayload {
        val payload = EncryptedPayload(
            iv = encryptedPayload.iv,
            ciphertext = encryptedPayload.ciphertext,
            schemaVersion = encryptedPayload.schemaVersion,
        )
        val result = cryptoService.decrypt(
            DecryptSecretRequest(
                payload = payload,
                associatedData = VaultCryptoAssociatedData.forPayload(
                    entryId = entryId,
                    payloadVersion = encryptedPayload.schemaVersion,
                ),
            ),
        )

        return when (result) {
            is CryptoOperationResult.Success -> DecryptedStoredPayload(
                schemaVersion = encryptedPayload.schemaVersion,
                plaintext = result.value,
            )
            is CryptoOperationResult.Failure -> throw VaultRepositoryException.DecryptionFailed
        }
    }
}

private data class DecryptedStoredPayload(
    val schemaVersion: Int,
    val plaintext: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DecryptedStoredPayload) return false
        return schemaVersion == other.schemaVersion &&
            plaintext.contentEquals(other.plaintext)
    }

    override fun hashCode(): Int {
        var result = schemaVersion
        result = 31 * result + plaintext.contentHashCode()
        return result
    }
}

sealed class VaultRepositoryException(
    message: String,
) : IllegalStateException(message) {
    data object EncryptionFailed : VaultRepositoryException("Vault payload encryption failed")
    data object DecryptionFailed : VaultRepositoryException("Vault payload decryption failed")
    data object PayloadDecodingFailed : VaultRepositoryException("Vault payload decoding failed")
}

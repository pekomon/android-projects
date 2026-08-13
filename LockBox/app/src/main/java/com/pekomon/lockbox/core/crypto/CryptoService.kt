package com.pekomon.lockbox.core.crypto

import com.pekomon.lockbox.domain.model.VaultEntryId
import java.nio.charset.StandardCharsets

data class EncryptedPayload(
    val iv: ByteArray,
    val ciphertext: ByteArray,
    val schemaVersion: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedPayload) return false
        return schemaVersion == other.schemaVersion &&
            iv.contentEquals(other.iv) &&
            ciphertext.contentEquals(other.ciphertext)
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + ciphertext.contentHashCode()
        result = 31 * result + schemaVersion
        return result
    }
}

data class EncryptSecretRequest(
    val plaintext: ByteArray,
    val associatedData: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptSecretRequest) return false
        return plaintext.contentEquals(other.plaintext) &&
            associatedData.contentEquals(other.associatedData)
    }

    override fun hashCode(): Int {
        var result = plaintext.contentHashCode()
        result = 31 * result + associatedData.contentHashCode()
        return result
    }
}

data class DecryptSecretRequest(
    val payload: EncryptedPayload,
    val associatedData: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DecryptSecretRequest) return false
        return payload == other.payload &&
            associatedData.contentEquals(other.associatedData)
    }

    override fun hashCode(): Int {
        var result = payload.hashCode()
        result = 31 * result + associatedData.contentHashCode()
        return result
    }
}

sealed interface CryptoOperationResult<out T> {
    data class Success<T>(
        val value: T,
    ) : CryptoOperationResult<T>

    data class Failure(
        val error: CryptoFailure,
    ) : CryptoOperationResult<Nothing>
}

sealed interface CryptoFailure {
    data object KeyUnavailable : CryptoFailure
    data object EncryptionFailed : CryptoFailure
    data object DecryptionFailed : CryptoFailure
    data object AuthenticationFailed : CryptoFailure
}

object VaultCryptoAssociatedData {
    fun forPayload(
        entryId: VaultEntryId,
        payloadVersion: Int,
    ): ByteArray = "${entryId.value}:$payloadVersion".toByteArray(StandardCharsets.UTF_8)
}

interface CryptoService {
    fun encrypt(
        request: EncryptSecretRequest,
    ): CryptoOperationResult<EncryptedPayload>

    fun decrypt(
        request: DecryptSecretRequest,
    ): CryptoOperationResult<ByteArray>
}

class NoOpCryptoService : CryptoService {
    override fun encrypt(
        request: EncryptSecretRequest,
    ): CryptoOperationResult<EncryptedPayload> = CryptoOperationResult.Success(
        EncryptedPayload(
            iv = request.associatedData,
            ciphertext = request.plaintext,
            schemaVersion = 1,
        ),
    )

    override fun decrypt(
        request: DecryptSecretRequest,
    ): CryptoOperationResult<ByteArray> = CryptoOperationResult.Success(request.payload.ciphertext)
}

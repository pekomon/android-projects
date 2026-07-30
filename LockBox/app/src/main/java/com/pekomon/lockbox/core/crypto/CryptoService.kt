package com.pekomon.lockbox.core.crypto

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

interface CryptoService {
    fun encrypt(
        plaintext: ByteArray,
        associatedData: ByteArray,
    ): EncryptedPayload

    fun decrypt(
        payload: EncryptedPayload,
        associatedData: ByteArray,
    ): ByteArray
}

class NoOpCryptoService : CryptoService {
    override fun encrypt(
        plaintext: ByteArray,
        associatedData: ByteArray,
    ): EncryptedPayload = EncryptedPayload(
        iv = associatedData,
        ciphertext = plaintext,
        schemaVersion = 1,
    )

    override fun decrypt(
        payload: EncryptedPayload,
        associatedData: ByteArray,
    ): ByteArray = payload.ciphertext
}

package com.pekomon.lockbox.core.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pekomon.lockbox.domain.model.VaultEntryId
import java.security.KeyStore
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidKeystoreCryptoServiceTest {
    private val keyAliases = mutableListOf<String>()

    @After
    fun tearDown() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        keyAliases.forEach { alias ->
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
        }
    }

    @Test
    fun encryptThenDecryptReturnsPlaintext() {
        val service = service()
        val plaintext = "correct horse battery staple".encodeToByteArray()
        val associatedData = associatedData("email", payloadVersion = 1)

        val encrypted = service.encrypt(
            EncryptSecretRequest(
                plaintext = plaintext,
                associatedData = associatedData,
                schemaVersion = 1,
            ),
        ).successValue()
        val decrypted = service.decrypt(
            DecryptSecretRequest(
                payload = encrypted,
                associatedData = associatedData,
            ),
        ).successValue()

        assertArrayEquals(plaintext, decrypted)
        assertFalse(plaintext.contentEquals(encrypted.ciphertext))
        assertTrue(encrypted.iv.size >= 12)
    }

    @Test
    fun encryptUsesFreshIvForEveryPayload() {
        val service = service()
        val plaintext = "same secret".encodeToByteArray()
        val associatedData = associatedData("same", payloadVersion = 1)

        val first = service.encrypt(
            EncryptSecretRequest(
                plaintext = plaintext,
                associatedData = associatedData,
            ),
        ).successValue()
        val second = service.encrypt(
            EncryptSecretRequest(
                plaintext = plaintext,
                associatedData = associatedData,
            ),
        ).successValue()

        assertFalse(first.iv.contentEquals(second.iv))
        assertFalse(first.ciphertext.contentEquals(second.ciphertext))
    }

    @Test
    fun decryptRejectsWrongAssociatedData() {
        val service = service()
        val encrypted = service.encrypt(
            EncryptSecretRequest(
                plaintext = "private note".encodeToByteArray(),
                associatedData = associatedData("note", payloadVersion = 1),
            ),
        ).successValue()

        val result = service.decrypt(
            DecryptSecretRequest(
                payload = encrypted,
                associatedData = associatedData("other-note", payloadVersion = 1),
            ),
        )

        assertFailure(CryptoFailure.AuthenticationFailed, result)
    }

    @Test
    fun decryptRejectsTamperedCiphertext() {
        val service = service()
        val encrypted = service.encrypt(
            EncryptSecretRequest(
                plaintext = "private note".encodeToByteArray(),
                associatedData = associatedData("note", payloadVersion = 1),
            ),
        ).successValue()
        val tamperedCiphertext = encrypted.ciphertext.copyOf().also { bytes ->
            bytes[0] = (bytes[0].toInt() xor 0x01).toByte()
        }

        val result = service.decrypt(
            DecryptSecretRequest(
                payload = encrypted.copy(ciphertext = tamperedCiphertext),
                associatedData = associatedData("note", payloadVersion = 1),
            ),
        )

        assertFailure(CryptoFailure.AuthenticationFailed, result)
    }

    @Test
    fun decryptWithoutExistingKeyFailsClosed() {
        val service = service(registerForCleanup = false)
        val result = service.decrypt(
            DecryptSecretRequest(
                payload = EncryptedPayload(
                    iv = ByteArray(12),
                    ciphertext = ByteArray(16),
                    schemaVersion = 1,
                ),
                associatedData = associatedData("missing", payloadVersion = 1),
            ),
        )

        assertFailure(CryptoFailure.KeyUnavailable, result)
    }

    private fun service(
        registerForCleanup: Boolean = true,
    ): AndroidKeystoreCryptoService {
        val alias = "lockbox_test_${UUID.randomUUID()}"
        if (registerForCleanup) {
            keyAliases += alias
        }
        return AndroidKeystoreCryptoService(keyAlias = alias)
    }

    private fun associatedData(
        entryId: String,
        payloadVersion: Int,
    ): ByteArray = VaultCryptoAssociatedData.forPayload(
        entryId = VaultEntryId(entryId),
        payloadVersion = payloadVersion,
    )

    private fun <T> CryptoOperationResult<T>.successValue(): T {
        assertTrue(this is CryptoOperationResult.Success)
        return (this as CryptoOperationResult.Success<T>).value
    }

    private fun assertFailure(
        expected: CryptoFailure,
        result: CryptoOperationResult<*>,
    ) {
        assertTrue(result is CryptoOperationResult.Failure)
        result as CryptoOperationResult.Failure
        assertTrue(result.error == expected)
    }
}

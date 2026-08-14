package com.pekomon.lockbox.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
private const val LOCKBOX_KEY_ALIAS = "lockbox_v1"
private const val AES_KEY_SIZE_BITS = 256
private const val GCM_TAG_SIZE_BITS = 128

class AndroidKeystoreCryptoService(
    private val keyAlias: String = LOCKBOX_KEY_ALIAS,
) : CryptoService {
    override fun encrypt(
        request: EncryptSecretRequest,
    ): CryptoOperationResult<EncryptedPayload> = try {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        cipher.updateAAD(request.associatedData)

        CryptoOperationResult.Success(
            EncryptedPayload(
                iv = cipher.iv,
                ciphertext = cipher.doFinal(request.plaintext),
                schemaVersion = request.schemaVersion,
            ),
        )
    } catch (exception: GeneralSecurityException) {
        CryptoOperationResult.Failure(CryptoFailure.EncryptionFailed)
    } catch (exception: IllegalStateException) {
        CryptoOperationResult.Failure(CryptoFailure.EncryptionFailed)
    }

    override fun decrypt(
        request: DecryptSecretRequest,
    ): CryptoOperationResult<ByteArray> {
        val secretKey = readExistingKey()
            ?: return CryptoOperationResult.Failure(CryptoFailure.KeyUnavailable)

        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                GCMParameterSpec(GCM_TAG_SIZE_BITS, request.payload.iv),
            )
            cipher.updateAAD(request.associatedData)

            CryptoOperationResult.Success(cipher.doFinal(request.payload.ciphertext))
        } catch (exception: AEADBadTagException) {
            CryptoOperationResult.Failure(CryptoFailure.AuthenticationFailed)
        } catch (exception: GeneralSecurityException) {
            CryptoOperationResult.Failure(CryptoFailure.DecryptionFailed)
        } catch (exception: IllegalStateException) {
            CryptoOperationResult.Failure(CryptoFailure.DecryptionFailed)
        }
    }

    private fun getOrCreateKey(): SecretKey = readExistingKey() ?: generateKey()

    private fun readExistingKey(): SecretKey? = try {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply { load(null) }
        keyStore.getKey(keyAlias, null) as? SecretKey
    } catch (exception: GeneralSecurityException) {
        null
    } catch (exception: IOException) {
        null
    }

    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE_PROVIDER,
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(AES_KEY_SIZE_BITS)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return keyGenerator.generateKey()
    }
}

package com.pekomon.lockbox.data.local

import com.pekomon.lockbox.domain.model.EntryKind
import com.pekomon.lockbox.domain.model.SecretPayload
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException

private const val PAYLOAD_SCHEMA_VERSION = 1

data class EncodedSecretPayload(
    val schemaVersion: Int,
    val kind: EntryKind,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncodedSecretPayload) return false
        return schemaVersion == other.schemaVersion &&
            kind == other.kind &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = schemaVersion
        result = 31 * result + kind.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

sealed interface StoredSecretPayload {
    val schemaVersion: Int
    val kind: EntryKind

    data class Login(
        override val schemaVersion: Int,
        val username: String,
        val password: String,
        val url: String,
        val notes: String,
    ) : StoredSecretPayload {
        override val kind: EntryKind = EntryKind.Login
    }

    data class SecureNote(
        override val schemaVersion: Int,
        val body: String,
    ) : StoredSecretPayload {
        override val kind: EntryKind = EntryKind.SecureNote
    }

    data class Card(
        override val schemaVersion: Int,
        val cardholder: String,
        val number: String,
        val expiry: String,
        val securityCode: String,
        val notes: String,
    ) : StoredSecretPayload {
        override val kind: EntryKind = EntryKind.Card
    }
}

class SecretPayloadCodec {
    fun encode(payload: SecretPayload): EncodedSecretPayload {
        val stored = payload.toStoredPayload()
        val bytes = ByteArrayOutputStream().use { byteStream ->
            DataOutputStream(byteStream).use { output ->
                output.writeInt(stored.schemaVersion)
                output.writeUTF(stored.kind.storageName)
                when (stored) {
                    is StoredSecretPayload.Login -> {
                        output.writeUTF(stored.username)
                        output.writeUTF(stored.password)
                        output.writeUTF(stored.url)
                        output.writeUTF(stored.notes)
                    }
                    is StoredSecretPayload.SecureNote -> {
                        output.writeUTF(stored.body)
                    }
                    is StoredSecretPayload.Card -> {
                        output.writeUTF(stored.cardholder)
                        output.writeUTF(stored.number)
                        output.writeUTF(stored.expiry)
                        output.writeUTF(stored.securityCode)
                        output.writeUTF(stored.notes)
                    }
                }
            }
            byteStream.toByteArray()
        }
        return EncodedSecretPayload(
            schemaVersion = stored.schemaVersion,
            kind = stored.kind,
            bytes = bytes,
        )
    }

    fun decode(encoded: EncodedSecretPayload): SecretPayload {
        val stored = try {
            DataInputStream(ByteArrayInputStream(encoded.bytes)).use { input ->
                val schemaVersion = input.readInt()
                if (schemaVersion != PAYLOAD_SCHEMA_VERSION) {
                    throw SecretPayloadCodecException.UnsupportedSchemaVersion(schemaVersion)
                }

                val kind = input.readKind()
                if (kind != encoded.kind) {
                    throw SecretPayloadCodecException.KindMismatch(
                        expected = encoded.kind,
                        actual = kind,
                    )
                }

                val storedPayload = when (kind) {
                    EntryKind.Login -> StoredSecretPayload.Login(
                        schemaVersion = schemaVersion,
                        username = input.readUTF(),
                        password = input.readUTF(),
                        url = input.readUTF(),
                        notes = input.readUTF(),
                    )
                    EntryKind.SecureNote -> StoredSecretPayload.SecureNote(
                        schemaVersion = schemaVersion,
                        body = input.readUTF(),
                    )
                    EntryKind.Card -> StoredSecretPayload.Card(
                        schemaVersion = schemaVersion,
                        cardholder = input.readUTF(),
                        number = input.readUTF(),
                        expiry = input.readUTF(),
                        securityCode = input.readUTF(),
                        notes = input.readUTF(),
                    )
                }

                if (input.available() != 0) {
                    throw SecretPayloadCodecException.MalformedPayload
                }

                storedPayload
            }
        } catch (exception: SecretPayloadCodecException) {
            throw exception
        } catch (exception: EOFException) {
            throw SecretPayloadCodecException.MalformedPayload
        } catch (exception: IOException) {
            throw SecretPayloadCodecException.MalformedPayload
        } catch (exception: IllegalArgumentException) {
            throw SecretPayloadCodecException.MalformedPayload
        }

        return stored.toDomainPayload()
    }

    private fun DataInputStream.readKind(): EntryKind {
        val storageName = readUTF()
        return EntryKind.entries.firstOrNull { it.storageName == storageName }
            ?: throw SecretPayloadCodecException.UnknownKind(storageName)
    }

    private fun SecretPayload.toStoredPayload(): StoredSecretPayload = when (this) {
        is SecretPayload.Login -> StoredSecretPayload.Login(
            schemaVersion = PAYLOAD_SCHEMA_VERSION,
            username = username,
            password = password,
            url = url,
            notes = notes,
        )
        is SecretPayload.SecureNote -> StoredSecretPayload.SecureNote(
            schemaVersion = PAYLOAD_SCHEMA_VERSION,
            body = body,
        )
        is SecretPayload.Card -> StoredSecretPayload.Card(
            schemaVersion = PAYLOAD_SCHEMA_VERSION,
            cardholder = cardholder,
            number = number,
            expiry = expiry,
            securityCode = securityCode,
            notes = notes,
        )
    }

    private fun StoredSecretPayload.toDomainPayload(): SecretPayload = when (this) {
        is StoredSecretPayload.Login -> SecretPayload.Login(
            username = username,
            password = password,
            url = url,
            notes = notes,
        )
        is StoredSecretPayload.SecureNote -> SecretPayload.SecureNote(
            body = body,
        )
        is StoredSecretPayload.Card -> SecretPayload.Card(
            cardholder = cardholder,
            number = number,
            expiry = expiry,
            securityCode = securityCode,
            notes = notes,
        )
    }

    private val EntryKind.storageName: String
        get() = when (this) {
            EntryKind.Login -> "login"
            EntryKind.SecureNote -> "secure_note"
            EntryKind.Card -> "card"
        }
}

sealed class SecretPayloadCodecException(
    message: String,
) : IllegalArgumentException(message) {
    data class UnsupportedSchemaVersion(
        val schemaVersion: Int,
    ) : SecretPayloadCodecException("Unsupported secret payload schema version: $schemaVersion")

    data class UnknownKind(
        val storageName: String,
    ) : SecretPayloadCodecException("Unknown secret payload kind: $storageName")

    data class KindMismatch(
        val expected: EntryKind,
        val actual: EntryKind,
    ) : SecretPayloadCodecException("Encoded payload kind $actual does not match stored kind $expected")

    data object MalformedPayload : SecretPayloadCodecException("Malformed secret payload")
}

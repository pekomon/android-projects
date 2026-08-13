package com.pekomon.lockbox.data.local

import com.pekomon.lockbox.domain.model.EntryKind
import com.pekomon.lockbox.domain.model.SecretPayload
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SecretPayloadCodecTest {
    private val codec = SecretPayloadCodec()

    @Test
    fun loginPayloadRoundTrips() {
        val payload = SecretPayload.Login(
            username = "ada@example.com",
            password = "correct horse battery staple",
            url = "https://mail.example.test",
            notes = "Recovery code elsewhere",
        )

        val encoded = codec.encode(payload)
        val decoded = codec.decode(encoded)

        assertEquals(1, encoded.schemaVersion)
        assertEquals(EntryKind.Login, encoded.kind)
        assertEquals(payload, decoded)
    }

    @Test
    fun secureNotePayloadRoundTrips() {
        val payload = SecretPayload.SecureNote(
            body = "Passport number 123456789",
        )

        val encoded = codec.encode(payload)
        val decoded = codec.decode(encoded)

        assertEquals(1, encoded.schemaVersion)
        assertEquals(EntryKind.SecureNote, encoded.kind)
        assertEquals(payload, decoded)
    }

    @Test
    fun cardPayloadRoundTrips() {
        val payload = SecretPayload.Card(
            cardholder = "Ada Lovelace",
            number = "4111111111111111",
            expiry = "09/29",
            securityCode = "123",
            notes = "Travel only",
        )

        val encoded = codec.encode(payload)
        val decoded = codec.decode(encoded)

        assertEquals(1, encoded.schemaVersion)
        assertEquals(EntryKind.Card, encoded.kind)
        assertEquals(payload, decoded)
    }

    @Test
    fun encodedPayloadUsesContentEqualityForBytes() {
        val encoded = codec.encode(SecretPayload.SecureNote(body = "Private body"))

        assertEquals(encoded, encoded.copy(bytes = encoded.bytes.copyOf()))
        assertArrayEquals(encoded.bytes, encoded.bytes.copyOf())
    }

    @Test
    fun decodeRejectsUnsupportedSchemaVersion() {
        val encoded = EncodedSecretPayload(
            schemaVersion = 2,
            kind = EntryKind.SecureNote,
            bytes = encodedBytes {
                writeInt(2)
                writeUTF("secure_note")
                writeUTF("Private body")
            },
        )

        assertThrows(SecretPayloadCodecException.UnsupportedSchemaVersion::class.java) {
            codec.decode(encoded)
        }
    }

    @Test
    fun decodeRejectsUnknownKind() {
        val encoded = EncodedSecretPayload(
            schemaVersion = 1,
            kind = EntryKind.SecureNote,
            bytes = encodedBytes {
                writeInt(1)
                writeUTF("wifi_password")
                writeUTF("Private body")
            },
        )

        assertThrows(SecretPayloadCodecException.UnknownKind::class.java) {
            codec.decode(encoded)
        }
    }

    @Test
    fun decodeRejectsKindMismatch() {
        val encoded = codec.encode(SecretPayload.SecureNote(body = "Private body"))

        assertThrows(SecretPayloadCodecException.KindMismatch::class.java) {
            codec.decode(encoded.copy(kind = EntryKind.Login))
        }
    }

    @Test
    fun decodeRejectsTruncatedPayload() {
        val encoded = codec.encode(
            SecretPayload.Login(
                username = "ada@example.com",
                password = "correct horse battery staple",
                url = "https://mail.example.test",
                notes = "Recovery code elsewhere",
            ),
        )

        assertThrows(SecretPayloadCodecException.MalformedPayload::class.java) {
            codec.decode(encoded.copy(bytes = encoded.bytes.copyOf(12)))
        }
    }

    @Test
    fun decodeRejectsTrailingBytes() {
        val encoded = codec.encode(SecretPayload.SecureNote(body = "Private body"))

        assertThrows(SecretPayloadCodecException.MalformedPayload::class.java) {
            codec.decode(encoded.copy(bytes = encoded.bytes + byteArrayOf(1)))
        }
    }

    private fun encodedBytes(
        write: DataOutputStream.() -> Unit,
    ): ByteArray = ByteArrayOutputStream().use { byteStream ->
        DataOutputStream(byteStream).use { output ->
            output.write()
        }
        byteStream.toByteArray()
    }
}

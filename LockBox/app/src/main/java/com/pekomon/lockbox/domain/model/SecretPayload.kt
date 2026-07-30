package com.pekomon.lockbox.domain.model

sealed interface SecretPayload {
    val kind: EntryKind

    data class Login(
        val username: String,
        val password: String,
        val url: String,
        val notes: String,
    ) : SecretPayload {
        override val kind: EntryKind = EntryKind.Login
    }

    data class SecureNote(
        val body: String,
    ) : SecretPayload {
        override val kind: EntryKind = EntryKind.SecureNote
    }

    data class Card(
        val cardholder: String,
        val number: String,
        val expiry: String,
        val securityCode: String,
        val notes: String,
    ) : SecretPayload {
        override val kind: EntryKind = EntryKind.Card
    }
}

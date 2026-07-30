package com.pekomon.lockbox.domain.validation

import com.pekomon.lockbox.domain.model.EntryKind

sealed interface VaultEntryDraft {
    val title: String
    val kind: EntryKind

    data class Login(
        override val title: String,
        val username: String,
        val password: String,
        val url: String,
        val notes: String,
    ) : VaultEntryDraft {
        override val kind: EntryKind = EntryKind.Login
    }

    data class SecureNote(
        override val title: String,
        val body: String,
    ) : VaultEntryDraft {
        override val kind: EntryKind = EntryKind.SecureNote
    }

    data class Card(
        override val title: String,
        val cardholder: String,
        val number: String,
        val expiry: String,
        val securityCode: String,
        val notes: String,
    ) : VaultEntryDraft {
        override val kind: EntryKind = EntryKind.Card
    }
}

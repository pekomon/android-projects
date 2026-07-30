package com.pekomon.lockbox.domain.model

import java.time.Instant

data class VaultEntryMetadata(
    val id: VaultEntryId,
    val title: String,
    val kind: EntryKind,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(title.isNotBlank()) { "Vault entry title must not be blank." }
        require(!updatedAt.isBefore(createdAt)) { "Updated timestamp must not be before created timestamp." }
    }
}

package com.pekomon.lockbox.domain.model

@JvmInline
value class VaultEntryId(val value: String) {
    init {
        require(value.isNotBlank()) { "Vault entry ID must not be blank." }
    }
}

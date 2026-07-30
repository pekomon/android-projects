package com.pekomon.lockbox.domain.model

data class VaultEntry(
    val metadata: VaultEntryMetadata,
    val payload: SecretPayload,
) {
    init {
        require(metadata.kind == payload.kind) {
            "Metadata kind ${metadata.kind} must match payload kind ${payload.kind}."
        }
    }
}

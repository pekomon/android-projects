package com.pekomon.lockbox.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.pekomon.lockbox.domain.model.EntryKind
import com.pekomon.lockbox.domain.model.VaultEntryId
import com.pekomon.lockbox.domain.model.VaultEntryMetadata
import java.time.Instant

@Entity(
    tableName = "vault_entry_metadata",
)
data class VaultEntryMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "entry_id")
    val entryId: String,
    val title: String,
    val kind: EntryKind,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
) {
    fun toDomain(): VaultEntryMetadata = VaultEntryMetadata(
        id = VaultEntryId(entryId),
        title = title,
        kind = kind,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(metadata: VaultEntryMetadata): VaultEntryMetadataEntity = VaultEntryMetadataEntity(
            entryId = metadata.id.value,
            title = metadata.title,
            kind = metadata.kind,
            createdAt = metadata.createdAt,
            updatedAt = metadata.updatedAt,
        )
    }
}

@Entity(
    tableName = "encrypted_secret_payload",
    foreignKeys = [
        ForeignKey(
            entity = VaultEntryMetadataEntity::class,
            parentColumns = ["entry_id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("entry_id"),
    ],
)
data class EncryptedSecretPayloadEntity(
    @PrimaryKey
    @ColumnInfo(name = "entry_id")
    val entryId: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val iv: ByteArray,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val ciphertext: ByteArray,
    @ColumnInfo(name = "schema_version")
    val schemaVersion: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedSecretPayloadEntity) return false
        return entryId == other.entryId &&
            iv.contentEquals(other.iv) &&
            ciphertext.contentEquals(other.ciphertext) &&
            schemaVersion == other.schemaVersion
    }

    override fun hashCode(): Int {
        var result = entryId.hashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + ciphertext.contentHashCode()
        result = 31 * result + schemaVersion
        return result
    }
}

data class StoredVaultEntry(
    @Embedded
    val metadata: VaultEntryMetadataEntity,
    @Relation(
        parentColumn = "entry_id",
        entityColumn = "entry_id",
    )
    val encryptedPayload: EncryptedSecretPayloadEntity,
)

package com.pekomon.lockbox.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultEntryDao {
    @Query("SELECT * FROM vault_entry_metadata ORDER BY updated_at DESC")
    fun observeMetadata(): Flow<List<VaultEntryMetadataEntity>>

    @Transaction
    @Query("SELECT * FROM vault_entry_metadata WHERE entry_id = :entryId")
    suspend fun getStoredEntry(entryId: String): StoredVaultEntry?

    @Transaction
    suspend fun saveStoredEntry(
        metadata: VaultEntryMetadataEntity,
        encryptedPayload: EncryptedSecretPayloadEntity,
    ) {
        upsertMetadata(metadata)
        upsertEncryptedPayload(encryptedPayload)
    }

    @Query("DELETE FROM vault_entry_metadata WHERE entry_id = :entryId")
    suspend fun deleteStoredEntry(entryId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMetadata(metadata: VaultEntryMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEncryptedPayload(encryptedPayload: EncryptedSecretPayloadEntity)
}

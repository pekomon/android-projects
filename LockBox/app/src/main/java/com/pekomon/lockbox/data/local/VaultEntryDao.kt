package com.pekomon.lockbox.data.local

import androidx.room.Dao
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
}

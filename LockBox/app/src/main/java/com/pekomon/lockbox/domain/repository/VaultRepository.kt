package com.pekomon.lockbox.domain.repository

import com.pekomon.lockbox.domain.model.VaultEntry
import com.pekomon.lockbox.domain.model.VaultEntryId
import com.pekomon.lockbox.domain.model.VaultEntryMetadata
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    val entries: Flow<List<VaultEntryMetadata>>

    suspend fun getEntry(id: VaultEntryId): VaultEntry?

    suspend fun saveEntry(entry: VaultEntry)

    suspend fun deleteEntry(id: VaultEntryId)
}

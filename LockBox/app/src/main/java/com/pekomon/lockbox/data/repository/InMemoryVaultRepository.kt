package com.pekomon.lockbox.data.repository

import com.pekomon.lockbox.domain.model.VaultEntry
import com.pekomon.lockbox.domain.model.VaultEntryId
import com.pekomon.lockbox.domain.model.VaultEntryMetadata
import com.pekomon.lockbox.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryVaultRepository : VaultRepository {
    private val mutex = Mutex()
    private val entryState = MutableStateFlow<Map<VaultEntryId, VaultEntry>>(emptyMap())

    override val entries: Flow<List<VaultEntryMetadata>> = entryState.map { entries ->
        entries.values
            .map { it.metadata }
            .sortedByDescending { it.updatedAt }
    }

    override suspend fun getEntry(id: VaultEntryId): VaultEntry? = mutex.withLock {
        entryState.value[id]
    }

    override suspend fun saveEntry(entry: VaultEntry) {
        mutex.withLock {
            entryState.value = entryState.value + (entry.metadata.id to entry)
        }
    }

    override suspend fun deleteEntry(id: VaultEntryId) {
        mutex.withLock {
            entryState.value = entryState.value - id
        }
    }
}

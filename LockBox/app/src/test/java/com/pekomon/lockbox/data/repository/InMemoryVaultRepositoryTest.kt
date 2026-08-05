package com.pekomon.lockbox.data.repository

import com.pekomon.lockbox.domain.model.EntryKind
import com.pekomon.lockbox.domain.model.SecretPayload
import com.pekomon.lockbox.domain.model.VaultEntry
import com.pekomon.lockbox.domain.model.VaultEntryId
import com.pekomon.lockbox.domain.model.VaultEntryMetadata
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryVaultRepositoryTest {
    @Test
    fun saveEntryCanBeReadById() = runTest {
        val repository = InMemoryVaultRepository()
        val entry = loginEntry(id = "github", title = "GitHub")

        repository.saveEntry(entry)

        assertEquals(entry, repository.getEntry(VaultEntryId("github")))
    }

    @Test
    fun entriesEmitMetadataSortedByMostRecentlyUpdated() = runTest {
        val repository = InMemoryVaultRepository()
        val older = loginEntry(
            id = "older",
            title = "Older",
            updatedAt = Instant.parse("2026-08-04T08:00:00Z"),
        )
        val newer = noteEntry(
            id = "newer",
            title = "Newer",
            updatedAt = Instant.parse("2026-08-04T10:00:00Z"),
        )

        repository.saveEntry(older)
        repository.saveEntry(newer)

        assertEquals(
            listOf(newer.metadata, older.metadata),
            repository.entries.first(),
        )
    }

    @Test
    fun saveEntryReplacesExistingEntryWithSameId() = runTest {
        val repository = InMemoryVaultRepository()
        val id = VaultEntryId("email")
        val original = loginEntry(
            id = id.value,
            title = "Email",
            updatedAt = Instant.parse("2026-08-04T08:00:00Z"),
        )
        val updated = original.copy(
            metadata = original.metadata.copy(
                title = "Personal email",
                updatedAt = Instant.parse("2026-08-04T11:00:00Z"),
            ),
            payload = SecretPayload.Login(
                username = "ada@example.com",
                password = "new password",
                url = "https://mail.example.test",
                notes = "Updated notes",
            ),
        )

        repository.saveEntry(original)
        repository.saveEntry(updated)

        assertEquals(updated, repository.getEntry(id))
        assertEquals(listOf(updated.metadata), repository.entries.first())
    }

    @Test
    fun deleteEntryRemovesEntryAndMetadata() = runTest {
        val repository = InMemoryVaultRepository()
        val entry = cardEntry(id = "card", title = "Travel card")

        repository.saveEntry(entry)
        repository.deleteEntry(entry.metadata.id)

        assertNull(repository.getEntry(entry.metadata.id))
        assertEquals(emptyList<VaultEntryMetadata>(), repository.entries.first())
    }

    @Test
    fun getEntryReturnsNullForMissingId() = runTest {
        val repository = InMemoryVaultRepository()

        assertNull(repository.getEntry(VaultEntryId("missing")))
    }

    private fun loginEntry(
        id: String,
        title: String,
        updatedAt: Instant = Instant.parse("2026-08-04T09:00:00Z"),
    ): VaultEntry = VaultEntry(
        metadata = metadata(
            id = id,
            title = title,
            kind = EntryKind.Login,
            updatedAt = updatedAt,
        ),
        payload = SecretPayload.Login(
            username = "ada@example.com",
            password = "correct horse battery staple",
            url = "https://example.test",
            notes = "Recovery code elsewhere",
        ),
    )

    private fun noteEntry(
        id: String,
        title: String,
        updatedAt: Instant,
    ): VaultEntry = VaultEntry(
        metadata = metadata(
            id = id,
            title = title,
            kind = EntryKind.SecureNote,
            updatedAt = updatedAt,
        ),
        payload = SecretPayload.SecureNote(
            body = "Private note body",
        ),
    )

    private fun cardEntry(
        id: String,
        title: String,
    ): VaultEntry = VaultEntry(
        metadata = metadata(
            id = id,
            title = title,
            kind = EntryKind.Card,
            updatedAt = Instant.parse("2026-08-04T09:00:00Z"),
        ),
        payload = SecretPayload.Card(
            cardholder = "Ada Lovelace",
            number = "4111111111111111",
            expiry = "09/29",
            securityCode = "123",
            notes = "Travel only",
        ),
    )

    private fun metadata(
        id: String,
        title: String,
        kind: EntryKind,
        updatedAt: Instant,
    ): VaultEntryMetadata = VaultEntryMetadata(
        id = VaultEntryId(id),
        title = title,
        kind = kind,
        createdAt = Instant.parse("2026-08-04T07:00:00Z"),
        updatedAt = updatedAt,
    )
}

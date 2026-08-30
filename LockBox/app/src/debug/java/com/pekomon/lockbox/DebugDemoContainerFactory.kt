package com.pekomon.lockbox

import android.content.Intent
import com.pekomon.lockbox.app.LockBoxAppContainer
import com.pekomon.lockbox.core.security.FakeBiometricAuthenticator
import com.pekomon.lockbox.core.security.InMemoryLockSession
import com.pekomon.lockbox.data.repository.InMemoryVaultRepository
import com.pekomon.lockbox.domain.model.EntryKind
import com.pekomon.lockbox.domain.model.SecretPayload
import com.pekomon.lockbox.domain.model.VaultEntry
import com.pekomon.lockbox.domain.model.VaultEntryId
import com.pekomon.lockbox.domain.model.VaultEntryMetadata
import java.time.Instant
import kotlinx.coroutines.runBlocking

object DebugDemoContainerFactory {
    private const val EXTRA_DEMO_MODE = "com.pekomon.lockbox.extra.DEMO_MODE"

    fun createOrNull(intent: Intent): LockBoxAppContainer? {
        if (!intent.getBooleanExtra(EXTRA_DEMO_MODE, false)) {
            return null
        }

        val repository = InMemoryVaultRepository()
        runBlocking {
            demoEntries().forEach { repository.saveEntry(it) }
        }

        return LockBoxAppContainer.fake(
            lockSession = InMemoryLockSession(),
            biometricAuthenticator = FakeBiometricAuthenticator(),
            vaultRepository = repository,
        )
    }

    private fun demoEntries(): List<VaultEntry> {
        val base = Instant.parse("2026-08-28T12:00:00Z")
        return listOf(
            VaultEntry(
                metadata = VaultEntryMetadata(
                    id = VaultEntryId("demo-github"),
                    title = "GitHub",
                    kind = EntryKind.Login,
                    createdAt = base.minusSeconds(86_400),
                    updatedAt = base,
                ),
                payload = SecretPayload.Login(
                    username = "pekka@example.test",
                    password = "correct-horse-demo",
                    url = "https://github.com",
                    notes = "Recovery codes stored offline.",
                ),
            ),
            VaultEntry(
                metadata = VaultEntryMetadata(
                    id = VaultEntryId("demo-travel"),
                    title = "Travel document",
                    kind = EntryKind.SecureNote,
                    createdAt = base.minusSeconds(172_800),
                    updatedAt = base.minusSeconds(3_600),
                ),
                payload = SecretPayload.SecureNote(
                    body = "Passport renewal reminder and embassy contact notes.",
                ),
            ),
            VaultEntry(
                metadata = VaultEntryMetadata(
                    id = VaultEntryId("demo-card"),
                    title = "Backup card",
                    kind = EntryKind.Card,
                    createdAt = base.minusSeconds(259_200),
                    updatedAt = base.minusSeconds(7_200),
                ),
                payload = SecretPayload.Card(
                    cardholder = "Pekka Example",
                    number = "4242424242424242",
                    expiry = "09/29",
                    securityCode = "123",
                    notes = "Demo-only card data for screenshots.",
                ),
            ),
        )
    }
}

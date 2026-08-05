package com.pekomon.lockbox.feature.vault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pekomon.lockbox.domain.model.EntryKind
import com.pekomon.lockbox.domain.model.VaultEntryMetadata
import com.pekomon.lockbox.domain.repository.VaultRepository
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun VaultRoute(
    vaultRepository: VaultRepository,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries by vaultRepository.entries.collectAsStateWithLifecycle(emptyList())

    VaultScreen(
        entries = entries,
        onAddClick = onAddClick,
        modifier = modifier,
    )
}

@Composable
internal fun VaultScreen(
    entries: List<VaultEntryMetadata>,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("vault_screen"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Vault",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                FloatingActionButton(
                    onClick = onAddClick,
                    modifier = Modifier.testTag("add_entry_button"),
                ) {
                    Text("+")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Private fields stay hidden until you open an entry.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (entries.isEmpty()) {
                VaultEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("vault_empty_state"),
                )
            } else {
                VaultEntryList(entries = entries)
            }
        }
    }
}

@Composable
private fun VaultEmptyState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Vault is empty",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your private entries will appear here after you add them.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VaultEntryList(
    entries: List<VaultEntryMetadata>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag("vault_entry_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        items(
            items = entries,
            key = { it.id.value },
        ) { entry ->
            VaultEntryRow(entry = entry)
        }
    }
}

@Composable
private fun VaultEntryRow(
    entry: VaultEntryMetadata,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vault_entry_row_${entry.id.value}"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = entry.kind.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Secret hidden",
                modifier = Modifier.testTag("vault_redacted_preview_${entry.id.value}"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Updated ${entry.updatedAt.atZone(ZoneId.systemDefault()).format(DateFormatter)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val DateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private val EntryKind.label: String
    get() = when (this) {
        EntryKind.Login -> "Login"
        EntryKind.SecureNote -> "Secure note"
        EntryKind.Card -> "Card"
    }

package com.pekomon.lockbox.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pekomon.lockbox.domain.model.EntryKind
import com.pekomon.lockbox.domain.model.SecretPayload
import com.pekomon.lockbox.domain.model.VaultEntry
import com.pekomon.lockbox.domain.model.VaultEntryId
import com.pekomon.lockbox.domain.repository.VaultRepository
import kotlinx.coroutines.launch

@Composable
fun EntryDetailRoute(
    entryId: String,
    vaultRepository: VaultRepository,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var entry by remember(entryId) { mutableStateOf<VaultEntry?>(null) }
    var isLoaded by remember(entryId) { mutableStateOf(false) }

    LaunchedEffect(entryId, vaultRepository) {
        entry = vaultRepository.getEntry(VaultEntryId(entryId))
        isLoaded = true
    }

    when {
        !isLoaded -> LoadingDetail(modifier = modifier)
        entry == null -> MissingDetail(onBack = onBack, modifier = modifier)
        else -> EntryDetailScreen(
            entry = requireNotNull(entry),
            onBack = onBack,
            onEdit = onEdit,
            onDelete = {
                scope.launch {
                    vaultRepository.deleteEntry(VaultEntryId(entryId))
                    onDeleted()
                }
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun LoadingDetail(
    modifier: Modifier = Modifier,
) {
    DetailSurface(
        modifier = modifier.testTag("entry_detail_loading"),
    ) {
        Text(
            text = "Loading entry",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun MissingDetail(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailSurface(
        modifier = modifier.testTag("entry_detail_missing"),
    ) {
        Text(
            text = "Entry not found",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "This vault entry is no longer available.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.testTag("detail_back_button"),
        ) {
            Text("Back")
        }
    }
}

@Composable
internal fun EntryDetailScreen(
    entry: VaultEntry,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isConfirmingDelete by remember { mutableStateOf(false) }

    if (isConfirmingDelete) {
        DeleteConfirmationDialog(
            title = entry.metadata.title,
            onConfirm = {
                isConfirmingDelete = false
                onDelete()
            },
            onDismiss = {
                isConfirmingDelete = false
            },
        )
    }

    DetailSurface(
        modifier = modifier.testTag("entry_detail_screen"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.testTag("detail_back_button"),
            ) {
                Text("Back")
            }
            Button(
                onClick = onEdit,
                modifier = Modifier.testTag("edit_entry_button"),
            ) {
                Text("Edit")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { isConfirmingDelete = true },
            modifier = Modifier.testTag("delete_entry_button"),
        ) {
            Text("Delete")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = entry.metadata.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.metadata.kind.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        when (val payload = entry.payload) {
            is SecretPayload.Login -> LoginDetail(payload)
            is SecretPayload.SecureNote -> SecretNoteDetail(payload)
            is SecretPayload.Card -> CardDetail(payload)
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delete entry?")
        },
        text = {
            Text("Delete $title from this vault?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_delete_button"),
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_button"),
            ) {
                Text("Cancel")
            }
        },
        modifier = Modifier.testTag("delete_confirmation_dialog"),
    )
}

@Composable
private fun DetailSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            content = content,
        )
    }
}

@Composable
private fun LoginDetail(payload: SecretPayload.Login) {
    SecretField(label = "Username", value = payload.username, testTag = "detail_username")
    SecretField(label = "Password", value = payload.password, testTag = "detail_password")
    SecretField(label = "URL", value = payload.url, testTag = "detail_url")
    SecretField(label = "Notes", value = payload.notes, testTag = "detail_notes")
}

@Composable
private fun SecretNoteDetail(payload: SecretPayload.SecureNote) {
    SecretField(label = "Note body", value = payload.body, testTag = "detail_note_body")
}

@Composable
private fun CardDetail(payload: SecretPayload.Card) {
    SecretField(label = "Cardholder", value = payload.cardholder, testTag = "detail_cardholder")
    SecretField(label = "Card number", value = payload.number, testTag = "detail_card_number")
    SecretField(label = "Expiry", value = payload.expiry, testTag = "detail_card_expiry")
    SecretField(label = "Security code", value = payload.securityCode, testTag = "detail_card_security_code")
    SecretField(label = "Notes", value = payload.notes, testTag = "detail_notes")
}

@Composable
private fun SecretField(
    label: String,
    value: String,
    testTag: String,
) {
    if (value.isBlank()) {
        return
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = value,
        modifier = Modifier.testTag(testTag),
        style = MaterialTheme.typography.bodyLarge,
    )
    Spacer(modifier = Modifier.height(14.dp))
}

private val EntryKind.label: String
    get() = when (this) {
        EntryKind.Login -> "Login"
        EntryKind.SecureNote -> "Secure note"
        EntryKind.Card -> "Card"
    }

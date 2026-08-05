package com.pekomon.lockbox.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pekomon.lockbox.domain.model.EntryKind
import com.pekomon.lockbox.domain.model.SecretPayload
import com.pekomon.lockbox.domain.model.VaultEntry
import com.pekomon.lockbox.domain.model.VaultEntryId
import com.pekomon.lockbox.domain.model.VaultEntryMetadata
import com.pekomon.lockbox.domain.repository.VaultRepository
import com.pekomon.lockbox.domain.validation.ValidationField
import com.pekomon.lockbox.domain.validation.VaultEntryDraft
import com.pekomon.lockbox.domain.validation.VaultEntryValidator
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun EntryEditorRoute(
    vaultRepository: VaultRepository,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    EntryEditorScreen(
        onCancel = onCancel,
        onSave = { draft ->
            scope.launch {
                vaultRepository.saveEntry(draft.toNewEntry())
                onSaved()
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun EntryEditorScreen(
    onSave: (VaultEntryDraft) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var kind by remember { mutableStateOf(EntryKind.Login) }
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var loginNotes by remember { mutableStateOf("") }
    var noteBody by remember { mutableStateOf("") }
    var cardholder by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardSecurityCode by remember { mutableStateOf("") }
    var cardNotes by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(emptyMap<ValidationField, String>()) }

    fun currentDraft(): VaultEntryDraft = when (kind) {
        EntryKind.Login -> VaultEntryDraft.Login(
            title = title,
            username = username,
            password = password,
            url = url,
            notes = loginNotes,
        )

        EntryKind.SecureNote -> VaultEntryDraft.SecureNote(
            title = title,
            body = noteBody,
        )

        EntryKind.Card -> VaultEntryDraft.Card(
            title = title,
            cardholder = cardholder,
            number = cardNumber,
            expiry = cardExpiry,
            securityCode = cardSecurityCode,
            notes = cardNotes,
        )
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("entry_editor_screen"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text(
                text = "New entry",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(20.dp))
            EntryKindPicker(
                selectedKind = kind,
                onKindSelected = {
                    kind = it
                    errors = emptyMap()
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            EditorField(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                testTag = "editor_title",
                error = errors[ValidationField.Title],
            )
            Spacer(modifier = Modifier.height(12.dp))
            when (kind) {
                EntryKind.Login -> LoginFields(
                    username = username,
                    onUsernameChange = { username = it },
                    password = password,
                    onPasswordChange = { password = it },
                    url = url,
                    onUrlChange = { url = it },
                    notes = loginNotes,
                    onNotesChange = { loginNotes = it },
                    errors = errors,
                )

                EntryKind.SecureNote -> SecureNoteFields(
                    body = noteBody,
                    onBodyChange = { noteBody = it },
                    errors = errors,
                )

                EntryKind.Card -> CardFields(
                    cardholder = cardholder,
                    onCardholderChange = { cardholder = it },
                    number = cardNumber,
                    onNumberChange = { cardNumber = it },
                    expiry = cardExpiry,
                    onExpiryChange = { cardExpiry = it },
                    securityCode = cardSecurityCode,
                    onSecurityCodeChange = { cardSecurityCode = it },
                    notes = cardNotes,
                    onNotesChange = { cardNotes = it },
                    errors = errors,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = {
                        val draft = currentDraft()
                        val validation = VaultEntryValidator.validate(draft)
                        if (validation.isValid) {
                            errors = emptyMap()
                            onSave(draft)
                        } else {
                            errors = validation.errors.associate { it.field to it.message }
                        }
                    },
                    modifier = Modifier.testTag("save_entry_button"),
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.testTag("cancel_editor_button"),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun EntryKindPicker(
    selectedKind: EntryKind,
    onKindSelected: (EntryKind) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EntryKind.entries.forEach { kind ->
            FilterChip(
                selected = selectedKind == kind,
                onClick = { onKindSelected(kind) },
                label = { Text(kind.label) },
                modifier = Modifier.testTag("kind_${kind.name}"),
            )
        }
    }
}

@Composable
private fun LoginFields(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    url: String,
    onUrlChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    errors: Map<ValidationField, String>,
) {
    EditorField(
        value = username,
        onValueChange = onUsernameChange,
        label = "Username",
        testTag = "editor_username",
        error = errors[ValidationField.Username],
    )
    Spacer(modifier = Modifier.height(12.dp))
    EditorField(
        value = password,
        onValueChange = onPasswordChange,
        label = "Password",
        testTag = "editor_password",
        error = errors[ValidationField.Password],
        isSecret = true,
    )
    Spacer(modifier = Modifier.height(12.dp))
    EditorField(
        value = url,
        onValueChange = onUrlChange,
        label = "URL",
        testTag = "editor_url",
        error = errors[ValidationField.Url],
    )
    Spacer(modifier = Modifier.height(12.dp))
    EditorField(
        value = notes,
        onValueChange = onNotesChange,
        label = "Notes",
        testTag = "editor_login_notes",
    )
}

@Composable
private fun SecureNoteFields(
    body: String,
    onBodyChange: (String) -> Unit,
    errors: Map<ValidationField, String>,
) {
    EditorField(
        value = body,
        onValueChange = onBodyChange,
        label = "Note body",
        testTag = "editor_note_body",
        error = errors[ValidationField.NoteBody],
    )
}

@Composable
private fun CardFields(
    cardholder: String,
    onCardholderChange: (String) -> Unit,
    number: String,
    onNumberChange: (String) -> Unit,
    expiry: String,
    onExpiryChange: (String) -> Unit,
    securityCode: String,
    onSecurityCodeChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    errors: Map<ValidationField, String>,
) {
    EditorField(
        value = cardholder,
        onValueChange = onCardholderChange,
        label = "Cardholder",
        testTag = "editor_cardholder",
        error = errors[ValidationField.Cardholder],
    )
    Spacer(modifier = Modifier.height(12.dp))
    EditorField(
        value = number,
        onValueChange = onNumberChange,
        label = "Card number",
        testTag = "editor_card_number",
        error = errors[ValidationField.CardNumber],
        isSecret = true,
    )
    Spacer(modifier = Modifier.height(12.dp))
    EditorField(
        value = expiry,
        onValueChange = onExpiryChange,
        label = "Expiry",
        testTag = "editor_card_expiry",
        error = errors[ValidationField.CardExpiry],
    )
    Spacer(modifier = Modifier.height(12.dp))
    EditorField(
        value = securityCode,
        onValueChange = onSecurityCodeChange,
        label = "Security code",
        testTag = "editor_card_security_code",
        error = errors[ValidationField.CardSecurityCode],
        isSecret = true,
    )
    Spacer(modifier = Modifier.height(12.dp))
    EditorField(
        value = notes,
        onValueChange = onNotesChange,
        label = "Notes",
        testTag = "editor_card_notes",
    )
}

@Composable
private fun EditorField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    testTag: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    isSecret: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        label = { Text(label) },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
        visualTransformation = if (isSecret) {
            PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
    )
}

private fun VaultEntryDraft.toNewEntry(): VaultEntry {
    val now = Instant.now()
    return VaultEntry(
        metadata = VaultEntryMetadata(
            id = VaultEntryId(UUID.randomUUID().toString()),
            title = title.trim(),
            kind = kind,
            createdAt = now,
            updatedAt = now,
        ),
        payload = toPayload(),
    )
}

private fun VaultEntryDraft.toPayload(): SecretPayload = when (this) {
    is VaultEntryDraft.Login -> SecretPayload.Login(
        username = username,
        password = password,
        url = url,
        notes = notes,
    )

    is VaultEntryDraft.SecureNote -> SecretPayload.SecureNote(
        body = body,
    )

    is VaultEntryDraft.Card -> SecretPayload.Card(
        cardholder = cardholder,
        number = number,
        expiry = expiry,
        securityCode = securityCode,
        notes = notes,
    )
}

private val EntryKind.label: String
    get() = when (this) {
        EntryKind.Login -> "Login"
        EntryKind.SecureNote -> "Secure note"
        EntryKind.Card -> "Card"
    }

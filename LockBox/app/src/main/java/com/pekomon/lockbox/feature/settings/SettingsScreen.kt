package com.pekomon.lockbox.feature.settings

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsScreen(
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button"),
                ) {
                    Text("Back")
                }
            }
            SettingsSummaryItem(
                title = "Unlock",
                body = "LockBox uses the Android system prompt with strong biometrics or your device credential. A screen lock must be enrolled before the vault can open.",
            )
            SettingsSummaryItem(
                title = "Storage",
                body = "Vault metadata is stored locally in Room. Secret fields are stored separately as encrypted payloads.",
            )
            SettingsSummaryItem(
                title = "Encryption",
                body = "Secret payloads use AES-GCM with a non-exportable Android Keystore key. The key is not authentication-bound in V1; system authentication gates the in-memory app session.",
            )
            SettingsSummaryItem(
                title = "Relock",
                body = "Cold starts begin locked, and backgrounding the process clears the unlocked app session.",
            )
            SettingsSummaryItem(
                title = "Backup",
                body = "Android backup and device-transfer extraction are disabled for vault data.",
            )
        }
    }
}

@Composable
private fun SettingsSummaryItem(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
    }
}

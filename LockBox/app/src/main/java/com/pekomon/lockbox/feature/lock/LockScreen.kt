package com.pekomon.lockbox.feature.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pekomon.lockbox.core.security.LockSession

@Composable
fun LockRoute(
    lockSession: LockSession,
) {
    LockScreen(
        onUnlockClick = lockSession::unlock,
    )
}

@Composable
internal fun LockScreen(
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("lock_screen"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "LockBox",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your vault is locked",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Unlock with your device credential before viewing private entries.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onUnlockClick,
                modifier = Modifier.testTag("unlock_button"),
            ) {
                Text("Unlock")
            }
        }
    }
}

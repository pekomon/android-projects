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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pekomon.lockbox.core.security.BiometricAuthenticator
import com.pekomon.lockbox.core.security.BiometricAvailabilityReader
import com.pekomon.lockbox.core.security.LockSession
import com.pekomon.lockbox.domain.model.BiometricAvailability

@Composable
fun LockRoute(
    lockSession: LockSession,
    availabilityReader: BiometricAvailabilityReader,
    authenticator: BiometricAuthenticator,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(lockSession, availabilityReader, authenticator) {
        LockController(
            lockSession = lockSession,
            availabilityReader = availabilityReader,
            authenticator = authenticator,
            scope = scope,
        )
    }
    val uiState by controller.uiState.collectAsStateWithLifecycle()

    LockScreen(
        uiState = uiState,
        onUnlockClick = controller::requestUnlock,
    )
}

@Composable
internal fun LockScreen(
    uiState: LockUiState,
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    LockScreenContent(
        statusText = uiState.statusText(),
        buttonText = if (uiState.promptState == PromptState.Authenticating) {
            "Authenticating"
        } else {
            "Unlock"
        },
        isUnlockEnabled = uiState.canRequestUnlock,
        stateTag = uiState.stateTag(),
        onUnlockClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onUnlockClick()
        },
        modifier = modifier,
    )
}

@Composable
private fun LockScreenContent(
    statusText: String,
    buttonText: String,
    isUnlockEnabled: Boolean,
    stateTag: String?,
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
            modifier = Modifier
                .padding(28.dp)
                .then(if (stateTag != null) Modifier.testTag(stateTag) else Modifier),
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
                text = statusText,
                modifier = Modifier.testTag("lock_status"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onUnlockClick,
                enabled = isUnlockEnabled,
                modifier = Modifier
                    .testTag("unlock_button")
                    .semantics {
                        contentDescription = "Unlock vault"
                    },
            ) {
                Text(buttonText)
            }
        }
    }
}

private fun LockUiState.statusText(): String = when {
    availability == BiometricAvailability.NoneEnrolled ->
        "No device credential is enrolled. Add a screen lock in Android settings to unlock LockBox."

    availability == BiometricAvailability.TemporarilyUnavailable ->
        "Unlock is temporarily unavailable. Try again in a moment."

    availability == BiometricAvailability.Unsupported ->
        "This device cannot protect LockBox with the required authentication."

    promptState == PromptState.Authenticating ->
        "Waiting for system authentication..."

    promptState == PromptState.FailedAttempt ->
        "Authentication was not recognized."

    promptState == PromptState.Canceled ->
        "Unlock canceled."

    promptState is PromptState.Error ->
        promptState.message

    else ->
        "Unlock with your device credential before viewing private entries."
}

private fun LockUiState.stateTag(): String? = when {
    availability != BiometricAvailability.Available -> "unavailable_state"
    promptState == PromptState.Authenticating -> "authenticating_state"
    promptState == PromptState.Canceled || promptState is PromptState.Error -> "auth_error_state"
    else -> null
}

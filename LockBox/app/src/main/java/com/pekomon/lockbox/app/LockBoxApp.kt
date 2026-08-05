package com.pekomon.lockbox.app

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pekomon.lockbox.core.security.BiometricAuthenticator
import com.pekomon.lockbox.feature.editor.EntryEditorRoute
import com.pekomon.lockbox.feature.lock.LockRoute
import com.pekomon.lockbox.feature.vault.VaultRoute

@Composable
fun LockBoxApp(
    appContainer: LockBoxAppContainer = LockBoxAppContainer.preview(),
    biometricAuthenticator: BiometricAuthenticator = appContainer.biometricAuthenticator,
) {
    val navController = rememberNavController()
    val isUnlocked by appContainer.lockSession.isUnlocked.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = LockBoxDestination.Lock.route,
    ) {
        composable(LockBoxDestination.Lock.route) {
            if (isUnlocked) {
                VaultRoute(
                    vaultRepository = appContainer.vaultRepository,
                    onAddClick = {
                        navController.navigate(LockBoxDestination.Editor.route)
                    },
                )
            } else {
                LockRoute(
                    lockSession = appContainer.lockSession,
                    availabilityReader = appContainer.biometricAvailabilityReader,
                    authenticator = biometricAuthenticator,
                )
            }
        }
        composable(LockBoxDestination.Editor.route) {
            if (isUnlocked) {
                EntryEditorRoute(
                    vaultRepository = appContainer.vaultRepository,
                    onSaved = {
                        navController.popBackStack(
                            route = LockBoxDestination.Lock.route,
                            inclusive = false,
                        )
                    },
                    onCancel = {
                        navController.popBackStack()
                    },
                )
            } else {
                LockRoute(
                    lockSession = appContainer.lockSession,
                    availabilityReader = appContainer.biometricAvailabilityReader,
                    authenticator = biometricAuthenticator,
                )
            }
        }
    }
}

enum class LockBoxDestination(val route: String) {
    Lock("lock"),
    Editor("editor"),
}

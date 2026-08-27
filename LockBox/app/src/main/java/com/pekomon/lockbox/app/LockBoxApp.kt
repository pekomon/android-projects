package com.pekomon.lockbox.app

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pekomon.lockbox.core.security.BiometricAuthenticator
import com.pekomon.lockbox.feature.detail.EntryDetailRoute
import com.pekomon.lockbox.feature.editor.EntryEditorRoute
import com.pekomon.lockbox.feature.lock.LockRoute
import com.pekomon.lockbox.feature.settings.SettingsRoute
import com.pekomon.lockbox.feature.vault.VaultRoute

@Composable
fun LockBoxApp(
    appContainer: LockBoxAppContainer = LockBoxAppContainer.preview(),
    biometricAuthenticator: BiometricAuthenticator = appContainer.biometricAuthenticator,
) {
    val navController = rememberNavController()
    val isUnlocked by appContainer.lockSession.isUnlocked.collectAsStateWithLifecycle()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(isUnlocked, currentRoute) {
        if (!isUnlocked && currentRoute != null && currentRoute != LockBoxDestination.Lock.route) {
            navController.navigate(LockBoxDestination.Lock.route) {
                popUpTo(LockBoxDestination.Lock.route) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

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
                    onSettingsClick = {
                        navController.navigate(LockBoxDestination.Settings.route)
                    },
                    onEntryClick = { entryId ->
                        navController.navigate(LockBoxDestination.Detail.createRoute(entryId))
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
        composable(LockBoxDestination.Detail.route) { backStackEntry ->
            if (isUnlocked) {
                val entryId = backStackEntry.arguments?.getString("entryId").orEmpty()
                EntryDetailRoute(
                    entryId = entryId,
                    vaultRepository = appContainer.vaultRepository,
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        navController.navigate(LockBoxDestination.Edit.createRoute(entryId))
                    },
                    onDeleted = {
                        navController.popBackStack(
                            route = LockBoxDestination.Lock.route,
                            inclusive = false,
                        )
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
                    entryId = null,
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
        composable(LockBoxDestination.Edit.route) { backStackEntry ->
            if (isUnlocked) {
                val entryId = backStackEntry.arguments?.getString("entryId").orEmpty()
                EntryEditorRoute(
                    entryId = entryId,
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
        composable(LockBoxDestination.Settings.route) {
            if (isUnlocked) {
                SettingsRoute(
                    onBack = { navController.popBackStack() },
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
    Detail("detail/{entryId}"),
    Edit("edit/{entryId}"),
    Settings("settings");

    fun createRoute(entryId: String): String = when (this) {
        Detail -> "detail/$entryId"
        Edit -> "edit/$entryId"
        else -> route
    }
}

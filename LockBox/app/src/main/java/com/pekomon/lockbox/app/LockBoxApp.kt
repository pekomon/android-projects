package com.pekomon.lockbox.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pekomon.lockbox.feature.lock.LockRoute

@Composable
fun LockBoxApp(
    appContainer: LockBoxAppContainer = LockBoxAppContainer.preview(),
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LockBoxDestination.Lock.route,
    ) {
        composable(LockBoxDestination.Lock.route) {
            LockRoute(lockSession = appContainer.lockSession)
        }
    }
}

enum class LockBoxDestination(val route: String) {
    Lock("lock"),
}

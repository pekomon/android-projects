package com.pekomon.cryptoapp.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Favorites : Screen("favorites", "Favorites")
    object Portfolio : Screen("portfolio", "Portfolio")
    object Settings : Screen("settings", "Settings")
} 
package com.example.pekomon.cryptoapp.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Favorites : Screen("favorites", "Favorites")
    object Settings : Screen("settings", "Settings")
} 
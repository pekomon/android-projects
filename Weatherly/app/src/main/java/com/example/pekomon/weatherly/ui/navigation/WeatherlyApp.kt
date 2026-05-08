package com.example.pekomon.weatherly.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pekomon.weatherly.core.ui.isMediumWidth
import com.example.pekomon.weatherly.feature.favorites.FavoritesRoute
import com.example.pekomon.weatherly.feature.home.HomeRoute
import com.example.pekomon.weatherly.feature.search.SearchRoute
import com.example.pekomon.weatherly.feature.settings.SettingsRoute

@Composable
fun WeatherlyApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val useNavigationRail = isMediumWidth()

    if (useNavigationRail) {
        Surface(modifier = modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                ) {
                    WeatherlyTopLevelDestination.entries.forEach { destination ->
                        NavigationRailItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (destination) {
                                        WeatherlyTopLevelDestination.Home -> Icons.Outlined.Home
                                        WeatherlyTopLevelDestination.Search -> Icons.Outlined.Search
                                        WeatherlyTopLevelDestination.Favorites -> Icons.Outlined.FavoriteBorder
                                        WeatherlyTopLevelDestination.Settings -> Icons.Outlined.Settings
                                    },
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
                NavHost(
                    navController = navController,
                    startDestination = WeatherlyTopLevelDestination.Home.route,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable(WeatherlyTopLevelDestination.Home.route) {
                        HomeRoute(contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp))
                    }
                    composable(WeatherlyTopLevelDestination.Search.route) {
                        SearchRoute(contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp))
                    }
                    composable(WeatherlyTopLevelDestination.Favorites.route) {
                        FavoritesRoute(contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp))
                    }
                    composable(WeatherlyTopLevelDestination.Settings.route) {
                        SettingsRoute(contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp))
                    }
                }
            }
        }
    } else {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                NavigationBar {
                    WeatherlyTopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (destination) {
                                        WeatherlyTopLevelDestination.Home -> Icons.Outlined.Home
                                        WeatherlyTopLevelDestination.Search -> Icons.Outlined.Search
                                        WeatherlyTopLevelDestination.Favorites -> Icons.Outlined.FavoriteBorder
                                        WeatherlyTopLevelDestination.Settings -> Icons.Outlined.Settings
                                    },
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = WeatherlyTopLevelDestination.Home.route,
                modifier = Modifier.padding(bottom = 0.dp),
            ) {
                composable(WeatherlyTopLevelDestination.Home.route) {
                    HomeRoute(contentPadding = innerPadding)
                }
                composable(WeatherlyTopLevelDestination.Search.route) {
                    SearchRoute(contentPadding = innerPadding)
                }
                composable(WeatherlyTopLevelDestination.Favorites.route) {
                    FavoritesRoute(contentPadding = innerPadding)
                }
                composable(WeatherlyTopLevelDestination.Settings.route) {
                    SettingsRoute(contentPadding = innerPadding)
                }
            }
        }
    }
}

enum class WeatherlyTopLevelDestination(val route: String, val label: String) {
    Home("home", "Home"),
    Search("search", "Search"),
    Favorites("favorites", "Favorites"),
    Settings("settings", "Settings"),
}

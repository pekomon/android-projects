package com.example.pekomon.cryptoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pekomon.cryptoapp.ui.CryptoViewModel
import com.example.pekomon.cryptoapp.ui.navigation.Screen
import com.example.pekomon.cryptoapp.ui.theme.CryptoAppTheme
import com.example.pekomon.cryptoapp.data.CryptoInfo
import com.example.pekomon.cryptoapp.ui.SplashScreen
import com.example.pekomon.cryptoapp.ui.components.SortMenu
import com.example.pekomon.cryptoapp.data.Currency

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoAppTheme {
                CryptoApp()
            }
        }
    }
}

@Composable
fun CryptoApp() {
    val navController = rememberNavController()
    val viewModel: CryptoViewModel = viewModel()
    var showSplash by remember { mutableStateOf(true) }
    
    if (showSplash) {
        SplashScreen(
            onSplashFinished = { showSplash = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    
                    val items = listOf(
                        Screen.Home to Icons.Default.Home,
                        Screen.Favorites to Icons.Default.Favorite,
                        Screen.Settings to Icons.Default.Settings
                    )
                    
                    items.forEach { (screen, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(viewModel = viewModel)
                }
                composable(Screen.Favorites.route) {
                    FavoritesScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Cryptocurrency Prices",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedButton(
            onClick = { /* Avaa lajitteluvalikon */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort by: ${viewModel.currentSortOption.displayName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort"
                )
            }
            SortMenu(
                currentSort = viewModel.currentSortOption,
                onSortSelected = { viewModel.updateSortOption(it) }
            )
        }
        
        when {
            viewModel.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            viewModel.error != null -> {
                Text(
                    text = viewModel.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                viewModel.sortedCryptos.forEach { crypto ->
                    CryptoPrice(
                        crypto = crypto,
                        isFavorite = viewModel.isFavorite(crypto.id),
                        onFavoriteClick = { viewModel.toggleFavorite(crypto.id) },
                        viewModel = viewModel
                    )
                }
            }
        }
        
        Button(
            onClick = { viewModel.fetchPrices() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Refresh Prices")
        }
    }
}

@Composable
fun FavoritesScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Favorite Cryptocurrencies",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedButton(
            onClick = { /* Avaa lajitteluvalikon */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort by: ${viewModel.currentSortOption.displayName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort"
                )
            }
            SortMenu(
                currentSort = viewModel.currentSortOption,
                onSortSelected = { viewModel.updateSortOption(it) }
            )
        }
        
        when {
            viewModel.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            viewModel.error != null -> {
                Text(
                    text = viewModel.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                val favorites = viewModel.sortedCryptos.filter { crypto -> 
                    viewModel.isFavorite(crypto.id)
                }
                
                if (favorites.isEmpty()) {
                    Text(
                        text = "No favorites yet",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    favorites.forEach { crypto ->
                        CryptoPrice(
                            crypto = crypto,
                            isFavorite = true,
                            onFavoriteClick = { viewModel.toggleFavorite(crypto.id) },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Currency",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Currency.entries.forEach { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currency == viewModel.selectedCurrency,
                                onClick = { viewModel.updateCurrency(currency) }
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currency == viewModel.selectedCurrency,
                            onClick = { viewModel.updateCurrency(currency) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${currency.name} (${currency.symbol})",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoPrice(
    crypto: CryptoInfo,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = crypto.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = crypto.symbol,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${viewModel.selectedCurrency.symbol}%.2f".format(crypto.price),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
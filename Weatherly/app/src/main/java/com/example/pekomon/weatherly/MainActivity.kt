package com.example.pekomon.weatherly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pekomon.weatherly.core.model.AppAppearanceMode
import com.example.pekomon.weatherly.data.repository.DataStoreSettingsRepository
import com.example.pekomon.weatherly.ui.navigation.WeatherlyApp
import com.example.pekomon.weatherly.ui.theme.WeatherlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsRepository = DataStoreSettingsRepository(applicationContext)
        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle()

            WeatherlyTheme(
                darkTheme = when (settings.appearanceMode) {
                    AppAppearanceMode.System -> androidx.compose.foundation.isSystemInDarkTheme()
                    AppAppearanceMode.Light -> false
                    AppAppearanceMode.Dark -> true
                },
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WeatherlyApp()
                }
            }
        }
    }
}

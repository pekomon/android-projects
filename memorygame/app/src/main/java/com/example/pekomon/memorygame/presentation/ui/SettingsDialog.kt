package com.example.pekomon.memorygame.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    initialEffectVolume: Float,
    initialMusicVolume: Float,
    onEffectVolumeChanged: (Float) -> Unit,
    onMusicVolumeChanged: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var effectsVolume by remember { mutableStateOf(initialEffectVolume) }
    var musicVolume by remember { mutableStateOf(initialMusicVolume) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(16.dp),
    ) {

        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Effect Volume")
            Slider(
                value = effectsVolume,
                onValueChange = {
                    effectsVolume = it
                    onEffectVolumeChanged(it)
                },
                valueRange = 0f..1f,
            )
            Text(text = "Music Volume")
            Slider(
                value = musicVolume,
                onValueChange = {
                    musicVolume = it
                },
                valueRange = 0f..1f,
            )
            Button(

                onClick = {
                    onEffectVolumeChanged(effectsVolume)
                    onMusicVolumeChanged(musicVolume)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        }

    }
}
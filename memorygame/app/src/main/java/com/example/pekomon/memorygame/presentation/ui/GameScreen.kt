package com.example.pekomon.memorygame.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pekomon.memorygame.presentation.ui.component.AnimatedMemoryCard
import com.example.pekomon.memorygame.presentation.viewmodel.GameViewModel

const val WIN_MESSAGE = "🎉 You Win! 🎉"

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val cards by viewModel.cards.collectAsState()
    val moves by viewModel.moves.collectAsState()
    val score by viewModel.score.collectAsState()
    val isGameWon by viewModel.isGameWon.collectAsState()
    val bestScore by viewModel.bestScore.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.startBackgroundMusic()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.releaseSoundManager()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startBackgroundMusic()
                Lifecycle.Event.ON_PAUSE -> viewModel.pauseBackgroundMusic()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { showSettingsDialog = true }
        ) {
            Text(text = "Settings")
        }

        if (showSettingsDialog) {
            val volumes = viewModel.getVolumes()
            SettingsDialog(
                initialEffectVolume = volumes.first,
                initialMusicVolume = volumes.second,
                onEffectVolumeChanged = { viewModel.setEffectsVolume(it) },
                onMusicVolumeChanged = { viewModel.setMusicVolume(it) },
                onDismiss = { showSettingsDialog = false}
            )
        }

        if (isGameWon) {
            Text(
                text = WIN_MESSAGE,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Green
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.restartGame() }
            ) {
                Text(text = "Play Again")
            }
        } else {

            Text(
                text = "Best Score: ${bestScore ?: "-"}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Moves: $moves",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Score: $score",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.padding(16.dp)
            ) {
                items(cards) { card ->
                    AnimatedMemoryCard(
                        card = card,
                        onClick = { viewModel.flipCard(card.id) }
                    )
                }
            }
        }
    }
}
package com.example.pekomon.memorygame.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pekomon.memorygame.R
import com.example.pekomon.memorygame.presentation.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val cards by viewModel.cards.collectAsState()
    val moves by viewModel.moves.collectAsState()
    val score by viewModel.score.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
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
                Card(modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp),
                    onClick = { viewModel.flipCard(card.id) }
                ) {
                    if (card.isFlipped) {
                        Image(
                            painter = painterResource(id = card.imageRes),
                            contentDescription = "Memory card",
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.card_back),
                            contentDescription = "Card upside down",
                        )
                    }
                }
            }
        }
    }
}
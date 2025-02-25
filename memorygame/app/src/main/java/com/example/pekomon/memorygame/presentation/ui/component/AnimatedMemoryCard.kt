package com.example.pekomon.memorygame.presentation.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pekomon.memorygame.R
import com.example.pekomon.memorygame.domain.model.Card

@Composable
fun AnimatedMemoryCard(
    card: Card,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "cardRotation"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 8 * density
            }
            .clickable { onClick() }
    ) {
        if (rotation > 90f) {
            Image(
                painter = painterResource(id = card.imageRes),
                contentDescription = "Memory card",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.card_back),
                contentDescription = "Card back",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
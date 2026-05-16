package com.pekomon.cryptoapp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen(
    viewModel: CryptoViewModel,
    onSplashFinished: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2000),
        finishedListener = { onSplashFinished() }
    )
    val coinScale by animateFloatAsState(
        targetValue = if (progress == 0f) 0.86f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val chartProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
    )
    
    LaunchedEffect(Unit) {
        viewModel.initialize()
        progress = 1f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF10201F),
                        Color(0xFF142C29),
                        Color(0xFF07100F)
                    )
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        SplashChart(
            progress = chartProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .align(Alignment.BottomCenter)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            CoinMark(
                modifier = Modifier
                    .size(112.dp)
                    .scale(coinScale)
            )

            Text(
                text = "CryptoApp",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFFF7F2DF),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Watch markets. Track holdings.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xB3F7F2DF)
            )
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp),
                color = Color(0xFF5AE6C4),
                trackColor = Color(0x335AE6C4)
            )
        }
    }
}

@Composable
private fun CoinMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.42f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFF2C94C), Color(0xFFB8841B)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color(0xFF10201F),
                radius = radius * 0.72f,
                center = center
            )
            drawCircle(
                color = Color(0xFFF2C94C),
                radius = radius,
                center = center,
                style = Stroke(width = 5.dp.toPx())
            )

            val chartPath = Path().apply {
                moveTo(size.width * 0.25f, size.height * 0.6f)
                lineTo(size.width * 0.42f, size.height * 0.48f)
                lineTo(size.width * 0.54f, size.height * 0.55f)
                lineTo(size.width * 0.72f, size.height * 0.36f)
            }
            drawPath(
                path = chartPath,
                color = Color(0xFF5AE6C4),
                style = Stroke(
                    width = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
private fun SplashChart(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val path = Path().apply {
            moveTo(0f, height * 0.72f)
            cubicTo(
                width * 0.18f,
                height * 0.56f,
                width * 0.28f,
                height * 0.9f,
                width * 0.44f,
                height * 0.6f
            )
            cubicTo(
                width * 0.58f,
                height * 0.34f,
                width * 0.7f,
                height * 0.5f,
                width * 0.82f,
                height * 0.24f
            )
            cubicTo(
                width * 0.9f,
                height * 0.08f,
                width * 0.96f,
                height * 0.18f,
                width,
                height * 0.1f
            )
        }

        drawPath(
            path = path,
            color = Color(0x225AE6C4),
            style = Stroke(
                width = 8.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
        drawPath(
            path = path,
            color = Color(0xFF5AE6C4).copy(alpha = progress.coerceIn(0f, 1f)),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

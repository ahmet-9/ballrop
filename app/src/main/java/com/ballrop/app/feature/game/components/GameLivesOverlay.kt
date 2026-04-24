package com.ballrop.app.feature.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ballrop.app.R
import com.ballrop.app.core.design.BallropColors
import kotlinx.coroutines.delay

private const val TOTAL_HEART_SLOTS = 5
private const val HEART_BURST_DURATION_MS = 900L

@Composable
fun GameLivesOverlay(
    remainingLives: Int,
    modifier: Modifier = Modifier,
    scale: Float = 1f
) {
    val safeLives = remainingLives.coerceIn(0, TOTAL_HEART_SLOTS)

    var previousLives by remember { mutableIntStateOf(safeLives) }
    var explodingIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(safeLives) {
        if (safeLives < previousLives) {
            explodingIndex = (previousLives - 1).coerceIn(0, TOTAL_HEART_SLOTS - 1)
            delay(HEART_BURST_DURATION_MS)
            explodingIndex = -1
        }
        previousLives = safeLives
    }

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        LivesCountBadge(
            lives = safeLives
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(TOTAL_HEART_SLOTS) { index ->
                HeartOverlaySlot(
                    isAlive = index < safeLives,
                    isExploding = index == explodingIndex
                )
            }
        }
    }
}

@Composable
private fun LivesCountBadge(
    lives: Int
) {
    Box(
        modifier = Modifier
            .background(
                color = BallropColors.Surface.copy(alpha = 0.52f),
                shape = CircleShape
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = lives.toString(),
            color = BallropColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun HeartOverlaySlot(
    isAlive: Boolean,
    isExploding: Boolean
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.heart_burst)
    )

    val burstProgress = remember { Animatable(0f) }

    LaunchedEffect(isExploding) {
        if (isExploding) {
            burstProgress.snapTo(0f)
            burstProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = HEART_BURST_DURATION_MS.toInt())
            )
        } else {
            burstProgress.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier.size(34.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = BallropColors.Surface.copy(alpha = if (isAlive) 0.42f else 0.20f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "♥",
                color = if (isAlive) {
                    BallropColors.Error
                } else {
                    BallropColors.TextMuted.copy(alpha = 0.35f)
                },
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .offset(y = (-1).dp)
                    .alpha(if (isExploding) 0f else 1f)
            )
        }

        if (isExploding && composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { burstProgress.value },
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        alpha = 1f
                        scaleX = 1f
                        scaleY = 1f
                    }
            )
        }
    }
}
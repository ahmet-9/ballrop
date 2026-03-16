package com.ballrop.app.feature.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.ballrop.app.core.design.BallropColors
import com.ballrop.app.core.design.BallropDimens

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@Composable
fun GameDebugPanel(
    currentLevel: Int,
    remainingLives: Int,
    checkpointStartLevel: Int,
    panelOffsetX: Float,
    panelOffsetY: Float,
    onPanelDrag: (Float, Float) -> Unit,
    onNextLevelClick: () -> Unit,
    onAddLivesClick: () -> Unit,
    onRestartFromCheckpointClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .offset {
                IntOffset(
                    x = panelOffsetX.roundToInt(),
                    y = panelOffsetY.roundToInt()
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onPanelDrag(dragAmount.x, dragAmount.y)
                }
            }
            .background(
                color = BallropColors.Surface.copy(alpha = 0.96f),
                shape = RoundedCornerShape(BallropDimens.Radius20)
            )
            .padding(BallropDimens.Space12),
        verticalArrangement = Arrangement.spacedBy(BallropDimens.Space10)
    ) {
        Text(
            text = "DEBUG PANEL",
            style = MaterialTheme.typography.labelLarge,
            color = BallropColors.TextPrimary
        )

        Text(
            text = "Level: $currentLevel  •  Hak: $remainingLives  •  Checkpoint: L$checkpointStartLevel",
            style = MaterialTheme.typography.bodySmall,
            color = BallropColors.TextSecondary
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space8)
        ) {
            DebugButton(
                text = "Level +1",
                onClick = onNextLevelClick
            )

            DebugButton(
                text = "Hak +10",
                onClick = onAddLivesClick
            )
        }

        DebugButton(
            text = "Checkpoint'ten Başlat",
            onClick = onRestartFromCheckpointClick
        )
    }
}

@Composable
private fun DebugButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .widthIn(min = BallropDimens.Space40)
            .background(
                color = BallropColors.SurfaceSoft,
                shape = RoundedCornerShape(BallropDimens.Radius16)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = BallropDimens.Space12,
                vertical = BallropDimens.Space10
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = BallropColors.TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}
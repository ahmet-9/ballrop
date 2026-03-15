package com.ballrop.app.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ballrop.app.core.design.BallropColors
import com.ballrop.app.core.design.BallropDimens
import com.ballrop.app.model.game.GameStatus
import kotlinx.coroutines.delay

@Composable
fun GameScreen() {
    val controller = remember { GameController() }
    val uiState = controller.uiState
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        controller.onAction(GameAction.StartGame)
    }

    LaunchedEffect(uiState.status) {
        if (uiState.status == GameStatus.PLAYING) {
            while (true) {
                controller.onAction(GameAction.Tick)
                delay(16L)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BallropColors.BackgroundPrimary)
            .statusBarsPadding()
            .padding(horizontal = BallropDimens.Space16)
            .padding(top = BallropDimens.Space12, bottom = BallropDimens.Space12)
    ) {
        GameTopBar(
            uiState = uiState
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = BallropDimens.Space12)
        ) {
            val canvasWidth = with(density) { maxWidth.toPx() }
            val canvasHeight = with(density) { maxHeight.toPx() }

            LaunchedEffect(canvasWidth, canvasHeight) {
                controller.onAction(
                    GameAction.UpdateCanvasSize(
                        width = canvasWidth,
                        height = canvasHeight
                    )
                )
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                GameCanvas(
                    modifier = Modifier.fillMaxSize(),
                    uiState = uiState
                )

                if (uiState.status == GameStatus.LEVEL_COMPLETED) {
                    LevelCompletedOverlay(
                        level = uiState.currentLevel
                    )
                }
            }
        }
    }
}

@Composable
private fun GameTopBar(
    uiState: GameUiState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space12)
    ) {
        InfoCard(
            title = "Skor",
            value = uiState.score.toString(),
            modifier = Modifier.weight(1f)
        )

        InfoCard(
            title = "Doğru",
            value = "${uiState.correctPlacedCount}/${uiState.levelConfig.targetCorrectCount}",
            modifier = Modifier.weight(1f)
        )

        InfoCard(
            title = "Level",
            value = uiState.currentLevel.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = BallropColors.SurfaceSoft,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(BallropDimens.Radius16)
            )
            .padding(
                horizontal = BallropDimens.Space12,
                vertical = BallropDimens.Space12
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BallropDimens.Space4)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = BallropColors.TextMuted
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = BallropColors.TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LevelCompletedOverlay(
    level: Int
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(
                    color = BallropColors.Surface.copy(alpha = 0.95f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(BallropDimens.Radius20)
                )
                .padding(
                    horizontal = BallropDimens.Space24,
                    vertical = BallropDimens.Space20
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BallropDimens.Space8)
        ) {
            Text(
                text = "LEVEL TAMAMLANDI",
                style = MaterialTheme.typography.titleLarge,
                color = BallropColors.Success,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Level $level başarıyla bitti.",
                style = MaterialTheme.typography.bodyLarge,
                color = BallropColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
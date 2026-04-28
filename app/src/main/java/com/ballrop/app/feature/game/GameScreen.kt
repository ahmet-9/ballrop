package com.ballrop.app.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ballrop.app.core.design.BallropColors
import com.ballrop.app.core.design.BallropDimens
import com.ballrop.app.core.feedback.AndroidGameFeedbackManager
import com.ballrop.app.core.feedback.GameFeedbackManager
import com.ballrop.app.core.storage.GameProgressStorage
import com.ballrop.app.model.game.GameStatus
import kotlinx.coroutines.delay

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue


import androidx.compose.runtime.mutableFloatStateOf

import com.ballrop.app.feature.game.components.GameDebugPanel


import androidx.compose.ui.text.font.FontWeight
import com.ballrop.app.core.feedback.GameFeedbackType


import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.mutableStateOf

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.width


import com.ballrop.app.feature.game.components.GameLivesOverlay



import androidx.compose.ui.graphics.Path

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.sp

import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
private const val DEBUG_PANEL_ENABLED = true

@Composable
fun GameScreen(
    startLevel: Int,
    onExitToMenu: () -> Unit
) {
    val context = LocalContext.current

    val progressStorage = remember { GameProgressStorage(context) }
    var checkpointStartLevel by remember { mutableIntStateOf(1) }

    var debugPanelOffsetX by remember { mutableFloatStateOf(0f) }
    var debugPanelOffsetY by remember { mutableFloatStateOf(0f) }

    val feedbackManager: GameFeedbackManager = remember {
        AndroidGameFeedbackManager(context)
    }

    val controller = remember { GameController() }
    val uiState = controller.uiState

    LaunchedEffect(uiState.status) {
        if (
            uiState.status == GameStatus.GAME_OVER ||
            uiState.status == GameStatus.LEVEL_COMPLETED
        ) {
            progressStorage.saveIfBetter(
                score = uiState.score,
                level = uiState.currentLevel
            )
        }

        if (uiState.status == GameStatus.GAME_OVER) {
            progressStorage.clearResumeProgress()
            progressStorage.saveCheckpointStartLevel(uiState.currentLevel)
            checkpointStartLevel = uiState.currentLevel
        }

        if (uiState.status == GameStatus.LEVEL_COMPLETED) {
            progressStorage.clearResumeProgress()
            progressStorage.saveCheckpointStartLevel(uiState.currentLevel + 1)
            checkpointStartLevel = uiState.currentLevel + 1
        }
    }
    LaunchedEffect(uiState.status, uiState.currentLevel) {
        if (uiState.status == GameStatus.LEVEL_COMPLETED) {
            val completedLevel = uiState.currentLevel

            if (completedLevel % 3 == 0) {
                val nextCheckpointStartLevel = completedLevel + 1
                progressStorage.saveCheckpointStartLevel(nextCheckpointStartLevel)
                checkpointStartLevel = nextCheckpointStartLevel
            } else {
                checkpointStartLevel = progressStorage.getCheckpointStartLevel()
            }
        }
    }

    LaunchedEffect(startLevel) {
        checkpointStartLevel = progressStorage.getContinueLevel()

        val sceneSnapshot = progressStorage.getResumeSceneSnapshot()

        if (sceneSnapshot != null) {
            controller.onAction(
                GameAction.StartFromSceneSnapshot(sceneSnapshot)
            )
        } else if (progressStorage.hasResumeProgress()) {
            controller.onAction(
                GameAction.StartFromProgress(
                    level = progressStorage.getResumeLevel(),
                    part = progressStorage.getResumePart(),
                    correctPlacedCount = progressStorage.getResumeCorrectPlacedCount(),
                    missedCount = progressStorage.getResumeMissedCount(),
                    wrongCount = progressStorage.getResumeWrongCount(),
                    flowBlockIndex = progressStorage.getResumeFlowBlockIndex(),
                    flowBlockSpawnCount = progressStorage.getResumeFlowBlockSpawnCount(),
                    flowShapeConsumed = progressStorage.getResumeFlowShapeConsumed(),
                    flowShapePreGapCompleted = progressStorage.getResumeFlowShapePreGapCompleted(),
                    flowShapePostGapCompleted = progressStorage.getResumeFlowShapePostGapCompleted()
                )
            )
        } else if (startLevel > 1) {
            controller.onAction(GameAction.StartFromLevel(startLevel))
        } else {
            controller.onAction(GameAction.ResetGame)
        }

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

    LaunchedEffect(uiState.feedbackEvents) {
        uiState.feedbackEvents.forEach { event ->
            feedbackManager.play(event.type)
            controller.onAction(GameAction.ConsumeFeedback(event.id))
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BallropColors.BackgroundPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
               // .padding(horizontal = BallropDimens.Space16)
                .padding(top = BallropDimens.Space12, bottom = BallropDimens.Space12)
        ) {
            GameHeader(
                uiState = uiState,
                onPauseClick = {
                    if (uiState.status == GameStatus.PLAYING) {
                        controller.onAction(GameAction.PauseGame)
                    } else if (uiState.status == GameStatus.PAUSED) {
                        controller.onAction(GameAction.ResumeGame)
                    }
                }
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 0.dp)
            ){
                val widthPx = constraints.maxWidth.toFloat()
                val heightPx = constraints.maxHeight.toFloat()

                LaunchedEffect(widthPx, heightPx) {
                    controller.onAction(
                        GameAction.UpdateCanvasSize(
                            width = widthPx,
                            height = heightPx
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(uiState.status) {
                            if (uiState.status != GameStatus.PLAYING) return@pointerInput

                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                controller.onAction(
                                    GameAction.PointerDown(
                                        x = down.position.x,
                                        y = down.position.y
                                    )
                                )

                                val pointerId = down.id

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == pointerId }
                                        ?: break

                                    if (!change.pressed) {
                                        controller.onAction(GameAction.PointerUp)
                                        break
                                    }

                                    controller.onAction(
                                        GameAction.PointerMove(
                                            x = change.position.x,
                                            y = change.position.y
                                        )
                                    )

                                    change.consume()
                                }
                            }
                        }
                ) {
                    GameCanvas(
                        modifier = Modifier.fillMaxSize(),
                        uiState = uiState
                    )

                    FeedbackPopups(uiState = uiState)

                    ImpactOverlay(
                        impactOverlay = uiState.impactOverlay
                    )

                    when (uiState.status) {
                        GameStatus.PAUSED -> {
                            PauseOverlay(
                                checkpointStartLevel = checkpointStartLevel,
                                onResumeClick = {
                                    controller.onAction(GameAction.ResumeGame)
                                },
                                onRestartClick = {
                                    val restartLevel = checkpointStartLevel.coerceAtLeast(1)
                                    controller.onAction(GameAction.StartFromLevel(restartLevel))
                                    controller.onAction(GameAction.StartGame)
                                },
                                onExitToMenuClick = {
                                    val sceneSnapshot = controller.currentSceneSnapshot()
                                    val flowSnapshot = controller.currentFlowSnapshot()
                                    progressStorage.saveResumeProgress(
                                        level = uiState.currentLevel,
                                        part = uiState.currentPart,
                                        correctPlacedCount = uiState.correctPlacedCount,
                                        missedCount = uiState.missedCount,
                                        wrongCount = uiState.wrongCount,
                                        maxMistakesAllowed = uiState.maxMistakesAllowed,
                                        targetCorrectCount = uiState.levelConfig.partOrFirst(uiState.currentPart).targetCorrectCount,
                                        flowBlockIndex = flowSnapshot.currentBlockIndex,
                                        flowBlockSpawnCount = flowSnapshot.currentBlockSpawnCount,
                                        flowShapeConsumed = flowSnapshot.shapeConsumedForCurrentBlock,
                                        flowShapePreGapCompleted = flowSnapshot.shapePreGapCompleted,
                                        flowShapePostGapCompleted = flowSnapshot.shapePostGapCompleted
                                    )
                                    progressStorage.saveResumeSceneSnapshot(sceneSnapshot)
                                    checkpointStartLevel = uiState.currentLevel
                                    onExitToMenu()
                                }
                            )
                        }

                        GameStatus.LEVEL_COMPLETED -> {
                            LevelCompleteOverlay(
                                uiState = uiState,
                                checkpointInfo = buildCheckpointInfo(
                                    completedLevel = uiState.currentLevel,
                                    savedCheckpointStartLevel = checkpointStartLevel
                                ),
                                onContinueClick = {
                                    if (uiState.currentLevel % 3 == 0) {
                                        val nextCheckpointStartLevel = uiState.currentLevel + 1
                                        progressStorage.saveCheckpointStartLevel(
                                            nextCheckpointStartLevel
                                        )
                                        checkpointStartLevel = nextCheckpointStartLevel
                                    }

                                    controller.onAction(GameAction.NextLevel)
                                    controller.onAction(GameAction.StartGame)
                                },
                                onExitToMenuClick = {
                                    if (uiState.currentLevel % 3 == 0) {
                                        val nextCheckpointStartLevel = uiState.currentLevel + 1
                                        progressStorage.saveCheckpointStartLevel(
                                            nextCheckpointStartLevel
                                        )
                                        checkpointStartLevel = nextCheckpointStartLevel
                                    }

                                    onExitToMenu()
                                }
                            )
                        }

                        GameStatus.GAME_OVER -> {
                            GameOverOverlay(
                                uiState = uiState,
                                checkpointStartLevel = checkpointStartLevel,
                                onRestartClick = {
                                    progressStorage.clearResumeProgress()
                                    progressStorage.saveCheckpointStartLevel(uiState.currentLevel)

                                    controller.onAction(GameAction.StartFromLevel(uiState.currentLevel))
                                    controller.onAction(GameAction.StartGame)
                                },
                                onExitToMenuClick = onExitToMenu
                            )
                        }

                        else -> Unit
                    }

                    if (uiState.status == GameStatus.COUNTDOWN) {
                        CountdownOverlay(
                            onFinished = {
                                controller.onAction(GameAction.FinishCountdown)
                            }
                        )
                    }
                    val showDebugPanel = true
                    if (showDebugPanel) {
                        if (DEBUG_PANEL_ENABLED) {
                            GameDebugPanel(
                                currentLevel = uiState.currentLevel,
                                remainingLives = uiState.remainingLives,
                                checkpointStartLevel = checkpointStartLevel,
                                panelOffsetX = debugPanelOffsetX,
                                panelOffsetY = debugPanelOffsetY,
                                onPanelDrag = { dragX, dragY ->
                                    debugPanelOffsetX += dragX
                                    debugPanelOffsetY += dragY
                                },
                                onNextLevelClick = {
                                    val totalPartCount = uiState.totalPartCount
                                    val isLastPart = uiState.currentPart >= totalPartCount

                                    if (isLastPart) {
                                        val nextLevel = uiState.currentLevel + 1
                                        val debugCheckpointStartLevel =
                                            calculateCheckpointStartLevelForLevel(nextLevel)

                                        progressStorage.saveCheckpointStartLevel(
                                            debugCheckpointStartLevel
                                        )
                                        checkpointStartLevel = debugCheckpointStartLevel

                                        controller.onAction(GameAction.NextLevel)
                                        controller.onAction(GameAction.StartGame)
                                    } else {
                                        val simulatedTarget =
                                            uiState.targetCorrectCountForActivePart

                                        repeat(simulatedTarget.coerceAtLeast(1)) {
                                            controller.onAction(GameAction.DebugAddCorrectCatch)
                                        }
                                    }
                                },
                                onAddLivesClick = {
                                    controller.onAction(GameAction.DebugAddLives)
                                },
                                onRestartFromCheckpointClick = {
                                    val restartLevel = checkpointStartLevel.coerceAtLeast(1)

                                    controller.onAction(GameAction.StartFromLevel(restartLevel))
                                    controller.onAction(GameAction.StartGame)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(BallropDimens.Space12)
                            )
                        }
                    }
                }
            }
        }




    }
    }




@Composable
private fun GameHeader(
    uiState: GameUiState,
    onPauseClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
    ) {
        Text(
            text = "Level: ${uiState.currentLevel}",
            style = MaterialTheme.typography.titleSmall,
            color = BallropColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 86.dp, top = 2.dp)
        )

        Text(
            text = "Bölüm: ${uiState.currentPart}/${uiState.totalPartCount}",
            style = MaterialTheme.typography.titleSmall,
            color = BallropColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 2.dp)
        )

        PauseIconButton(
            isPaused = uiState.status == GameStatus.PAUSED,
            enabled = uiState.status == GameStatus.PLAYING || uiState.status == GameStatus.PAUSED,
            onClick = onPauseClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 2.dp, top = 0.dp)
        )

        GameLivesOverlay(
            remainingLives = uiState.remainingLives,
            scale = 1.25f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 35.dp, bottom = 2.dp, top = 2.dp)
        )
    }
}

@Composable
private fun PauseIconButton(
    isPaused: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isPaused) {
                    BallropColors.SurfaceSoft.copy(alpha = 0.96f)
                } else {
                    BallropColors.Surface.copy(alpha = 0.96f)
                }
            )
            .clickable(enabled = enabled) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isPaused) {
            PlayGlyph()
        } else {
            PauseGlyph()
        }
    }
}

@Composable
private fun PauseGlyph() {
    Box(
        modifier = Modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 5.dp, height = 18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(BallropColors.TextPrimary)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(width = 5.dp, height = 18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(BallropColors.TextPrimary)
            )
        }
    }
}
@Composable
private fun PlayGlyph() {
    Box(
        modifier = Modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(width = 16.dp, height = 18.dp)
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(0f, size.height)
                close()
            }

            drawPath(
                path = path,
                color = BallropColors.TextPrimary
            )
        }
    }
}
@Composable
private fun HeaderInfoText(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = BallropColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}















@Composable
private fun TopBarTextItem(
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space4)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            color = BallropColors.TextSecondary
        )

        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = BallropColors.TextPrimary
        )
    }
}

@Composable
private fun PauseButton(
    isPaused: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = if (enabled) {
                    BallropColors.SurfaceSoft
                } else {
                    BallropColors.SurfaceSoft.copy(alpha = 0.55f)
                },
                shape = RoundedCornerShape(BallropDimens.Radius16)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = BallropDimens.Space12,
                vertical = BallropDimens.Space12
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isPaused) "Devam" else "Dur",
            style = MaterialTheme.typography.labelLarge,
            color = BallropColors.TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    val bgColor = when {
        highlighted && title == "Hak" -> BallropColors.Error.copy(alpha = 0.20f)
        highlighted -> BallropColors.BasketBlue.copy(alpha = 0.18f)
        else -> BallropColors.SurfaceSoft
    }

    val borderColor = when {
        highlighted && title == "Hak" -> BallropColors.Error.copy(alpha = 0.45f)
        highlighted -> BallropColors.BasketBlue.copy(alpha = 0.45f)
        else -> BallropColors.White.copy(alpha = 0.08f)
    }

    Column(
        modifier = modifier
            .background(
                color = bgColor,
                shape = RoundedCornerShape(BallropDimens.Radius16)
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .background(
                    color = borderColor,
                    shape = RoundedCornerShape(100.dp)
                )
                .padding(vertical = 1.dp)
        )
    }
}

@Composable
private fun FeedbackPopups(
    uiState: GameUiState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        uiState.feedbackPopups.forEach { popup ->
            val chipColor = when {
                popup.isCombo -> BallropColors.Warning.copy(alpha = 0.92f)
                popup.isPositive -> BallropColors.Success.copy(alpha = 0.92f)
                else -> BallropColors.Error.copy(alpha = 0.92f)
            }

            val textColor = BallropColors.White

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = if (popup.isCombo) {
                            popup.x - 86f
                        } else {
                            popup.x - 42f
                        }

                        translationY = popup.y
                        alpha = popup.alpha
                    }
                    .scale(popup.scale)
                    .background(
                        color = chipColor,
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(
                        horizontal = if (popup.isCombo) 14.dp else 12.dp,
                        vertical = if (popup.isCombo) 8.dp else 6.dp
                    )
            ) {
                Text(
                    text = popup.text,
                    style = if (popup.isCombo) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.labelLarge
                    },
                    textAlign = TextAlign.Center,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ImpactOverlay(
    impactOverlay: GameImpactOverlay?
) {
    if (impactOverlay == null) return

    val color = when (impactOverlay.type) {
        GameFeedbackType.CORRECT -> BallropColors.Success
        GameFeedbackType.WRONG -> BallropColors.Error
        GameFeedbackType.MISS -> BallropColors.Warning
        GameFeedbackType.LEVEL_COMPLETE -> BallropColors.Warning
        GameFeedbackType.GAME_OVER -> BallropColors.Error
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = impactOverlay.alpha * 0.28f))
    )
}

@Composable
private fun LevelCompleteOverlay(
    uiState: GameUiState,
    checkpointInfo: CheckpointInfo,
    onContinueClick: () -> Unit,
    onExitToMenuClick: () -> Unit
) {
    val nextLevel = uiState.currentLevel + 1

    CenterOverlayCard(
        title = "Level ${uiState.currentLevel} Tamamlandı",
        isCelebration = true,
        subtitle = if (checkpointInfo.unlockedNow) {
            "Temiz geçtiysen ödülünü aldın. Yeni checkpoint hazır."
        } else {
            "Ritmi bozma. Sıradaki level daha hızlı gelecek."
        }
    ) {
        SummaryCard(
            title = "Sıradaki Level",
            value = nextLevel.toString(),
            modifier = Modifier.fillMaxWidth()
        )

        SummaryCard(
            title = "Checkpoint",
            value = if (checkpointInfo.unlockedNow) {
                "Level ${checkpointInfo.resumeLevel} açıldı"
            } else {
                "Sonraki checkpoint için ${checkpointInfo.levelsUntilNextCheckpoint} level kaldı"
            },
            modifier = Modifier.fillMaxWidth(),
            highlighted = checkpointInfo.unlockedNow
        )

        SummaryCard(
            title = "Devam Bilgisi",
            value = if (checkpointInfo.unlockedNow) {
                "Kaybedersen Level ${checkpointInfo.resumeLevel} noktasından dönersin."
            } else {
                "Şimdilik eski checkpoint korunuyor. Devam edip yeni blok açman lazım."
            },
            modifier = Modifier.fillMaxWidth()
        )

        OverlayButton(
            text = "Devam Et",
            primary = true,
            onClick = onContinueClick
        )

        OverlayButton(
            text = "Ana Menü",
            primary = false,
            onClick = onExitToMenuClick
        )
    }
}

@Composable
private fun PauseOverlay(
    checkpointStartLevel: Int,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitToMenuClick: () -> Unit
) {
    val hasCheckpoint = checkpointStartLevel > 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BallropColors.White.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = BallropDimens.Space20)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF70A2C67),
                            Color(0xF7061B46),
                            Color(0xF702102D)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2EA6FF),
                            Color(0xAA1B7CFF),
                            Color(0x66305CFF)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x5531A8FF),
                                Color(0x2200D1FF),
                                Color(0xFF071A3D)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xAA2EA6FF),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = BallropColors.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = "Oyun Duraklatıldı",
                    color = BallropColors.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Hazırsan 3 saniyelik geri sayımdan sonra kaldığın yerden devam edersin.",
                    color = BallropColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PauseInfoChip(
                    title = "Devam",
                    value = if (hasCheckpoint) {
                        "Level $checkpointStartLevel"
                    } else {
                        "Level 1"
                    },
                    modifier = Modifier.weight(1f)
                )

                PauseInfoChip(
                    title = "Başlatma",
                    value = "3 • 2 • 1",
                    modifier = Modifier.weight(1f)
                )
            }

            PausePrimaryButton(
                text = "Devam Et",
                icon = Icons.Filled.PlayArrow,
                onClick = onResumeClick
            )

            PauseSecondaryButton(
                text = "Ana Menü",
                onClick = onExitToMenuClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun PauseInfoChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D2A63),
                        Color(0xFF071C46),
                        Color(0xFF04132F)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0x442EA6FF),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            color = BallropColors.TextMuted,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = value,
            color = BallropColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PausePrimaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2EA6FF),
                        Color(0xFF0E72D8),
                        Color(0xFF07458F)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xCCB9E6FF),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BallropColors.White,
                modifier = Modifier.size(21.dp)
            )

            Text(
                text = text,
                color = BallropColors.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun PauseSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D2A63),
                        Color(0xFF071C46),
                        Color(0xFF04132F)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0x552EA6FF),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = BallropColors.TextPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GameOverOverlay(
    uiState: GameUiState,
    checkpointStartLevel: Int,
    onRestartClick: () -> Unit,
    onExitToMenuClick: () -> Unit
) {
    val reachedLevel = uiState.sessionSummary.reachedLevel

    CenterOverlayCard(
        title = "Run Bitti",
        isGameOver = true,
        subtitle = "Level $reachedLevel noktasına kadar geldin."
    ) {
        SummaryCard(
            title = "Ulaşılan Level",
            value = reachedLevel.toString(),
            modifier = Modifier.fillMaxWidth()
        )

        SummaryCard(
            title = "Checkpoint",
            value = if (checkpointStartLevel > 1) {
                "Level $checkpointStartLevel"
            } else {
                "Henüz açılmadı"
            },
            modifier = Modifier.fillMaxWidth(),
            highlighted = checkpointStartLevel > 1
        )

        SummaryCard(
            title = "Devam",
            value = if (checkpointStartLevel > 1) {
                "Tekrar oynarsan Level $checkpointStartLevel noktasından başlarsın."
            } else {
                "Yeni oyun Level 1'den başlayacak."
            },
            modifier = Modifier.fillMaxWidth()
        )

        OverlayButton(
            text = if (checkpointStartLevel > 1) {
                "Checkpoint'ten Devam Et"
            } else {
                "Tekrar Oyna"
            },
            primary = true,
            onClick = onRestartClick
        )

        OverlayButton(
            text = "Ana Menü",
            primary = false,
            onClick = onExitToMenuClick
        )
    }
}

@Composable
private fun CenterOverlayCard(
    title: String,
    subtitle: String,
    isCelebration: Boolean = false,
    isGameOver: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var entered by remember { mutableStateOf(false) }
    var celebrationPulseUp by remember { mutableStateOf(false) }
    var celebrationPulseDown by remember { mutableStateOf(false) }
    var shakePhase by remember { mutableStateOf(0f) }
    var effectFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(40)
        entered = true

        if (isCelebration) {
            celebrationPulseUp = true
            delay(140)
            celebrationPulseDown = true
            delay(220)
            effectFinished = true
        } else if (isGameOver) {
            val shakePattern = listOf(
                14f, -14f, 12f, -12f, 10f, -10f, 8f, -8f,
                6f, -6f, 4f, -4f, 2f, -2f, 0f
            )

            shakePattern.forEach { value ->
                shakePhase = value
                delay(130)
            }

            effectFinished = true
            shakePhase = 0f
        } else {
            effectFinished = true
        }
    }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(
            durationMillis = 260,
            easing = FastOutSlowInEasing
        ),
        label = "overlayAlpha"
    )

    val cardBaseScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.88f,
        animationSpec = tween(
            durationMillis = 280,
            easing = FastOutSlowInEasing
        ),
        label = "cardBaseScale"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "cardAlpha"
    )

    val celebrationScale by animateFloatAsState(
        targetValue = when {
            !isCelebration -> 1f
            effectFinished -> 1f
            celebrationPulseDown -> 0.98f
            celebrationPulseUp -> 1.08f
            else -> 1f
        },
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "celebrationScale"
    )

    val gameOverShake by animateFloatAsState(
        targetValue = if (isGameOver && !effectFinished) shakePhase else 0f,
        animationSpec = tween(
            durationMillis = 70,
            easing = FastOutSlowInEasing
        ),
        label = "gameOverShake"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BallropColors.Black.copy(alpha = 0.45f * overlayAlpha)),
        contentAlignment = Alignment.Center
    ) {
        if (isCelebration && !effectFinished) {
            CelebrationSparkles()
        }

        Column(
            modifier = Modifier
                .padding(horizontal = BallropDimens.Space20)
                .graphicsLayer {
                    alpha = cardAlpha
                    scaleX = cardBaseScale * celebrationScale
                    scaleY = cardBaseScale * celebrationScale
                    translationX = gameOverShake
                }
                .clip(RoundedCornerShape(BallropDimens.Radius24))
                .background(BallropColors.Surface.copy(alpha = 0.98f))
                .border(
                    width = 1.dp,
                    color = BallropColors.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(BallropDimens.Radius24)
                )
                .padding(BallropDimens.Space20),
            verticalArrangement = Arrangement.spacedBy(BallropDimens.Space14)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = BallropColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = BallropColors.TextSecondary
            )

            content()
        }
    }
}

@Composable
private fun CelebrationSparkles() {
    val sparkleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "sparkleAlpha"
    )

    val sparkleDots = listOf(
        Triple(0.18f, 0.22f, BallropColors.Warning),
        Triple(0.30f, 0.14f, BallropColors.Success),
        Triple(0.74f, 0.18f, BallropColors.Warning),
        Triple(0.84f, 0.26f, BallropColors.Success),
        Triple(0.20f, 0.78f, BallropColors.Warning),
        Triple(0.82f, 0.74f, BallropColors.Success),
        Triple(0.12f, 0.52f, BallropColors.White),
        Triple(0.88f, 0.50f, BallropColors.White)
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        sparkleDots.forEachIndexed { index, dot ->
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = dot.first * widthPx
                        translationY = dot.second * heightPx
                        alpha = sparkleAlpha
                    }
                    .size(if (index % 2 == 0) 10.dp else 7.dp)
                    .background(
                        color = dot.third.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(999.dp)
                    )
            )
        }
    }
}

@Composable
private fun OverlayButton(
    text: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (primary) {
        BallropColors.TextPrimary
    } else {
        BallropColors.SurfaceSoft
    }

    val contentColor = if (primary) {
        BallropColors.BackgroundPrimary
    } else {
        BallropColors.TextPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BallropDimens.Radius16))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = BallropDimens.Space14),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    val backgroundColor = if (highlighted) {
        BallropColors.Success.copy(alpha = 0.18f)
    } else {
        BallropColors.SurfaceSoft
    }

    Column(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(BallropDimens.Radius16)
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

private data class ResultTone(
    val title: String,
    val subtitle: String
)

private fun buildGameOverTone(uiState: GameUiState): ResultTone {
    val accuracy = uiState.sessionSummary.accuracyPercent
    val bestCombo = uiState.sessionSummary.bestCombo
    val reachedLevel = uiState.sessionSummary.reachedLevel

    return when {
        accuracy >= 85 && bestCombo >= 6 -> {
            ResultTone(
                title = "ÇOK İYİ OYNADIN",
                subtitle = "Temiz oynadın. Birkaç hata yüzünden düştün ama ritim baya iyi."
            )
        }

        accuracy >= 70 && reachedLevel >= 3 -> {
            ResultTone(
                title = "İYİ GİDİYORDUN",
                subtitle = "Temel ritmi kurmuşsun. Biraz daha dikkatle seri çok daha yukarı çıkar."
            )
        }

        bestCombo >= 4 -> {
            ResultTone(
                title = "RİTİMİ YAKALAMIŞTIN",
                subtitle = "Seri potansiyelin var. Dağılmadan oynarsan bu run çok daha ileri gider."
            )
        }

        else -> {
            ResultTone(
                title = "OYUN BİTTİ",
                subtitle = "Mantık sende. Şimdi daha temiz oynayıp ritmi koruman lazım."
            )
        }
    }
}

private fun buildLevelCompleteTone(uiState: GameUiState): ResultTone {
    val accuracy = uiState.accuracyPercent
    val combo = uiState.bestCombo

    return when {
        accuracy >= 90 && combo >= 5 -> {
            ResultTone(
                title = "MÜKEMMEL TEMİZLİK",
                subtitle = "Bu leveli ezmişsin. Sıradaki bölüm daha sert gelecek."
            )
        }

        accuracy >= 75 -> {
            ResultTone(
                title = "LEVEL TAMAMLANDI",
                subtitle = "Güzel tempo. Şimdi baskı biraz daha artacak."
            )
        }

        else -> {
            ResultTone(
                title = "LEVEL GEÇİLDİ",
                subtitle = "Geçtin ama rahatlama. Sonraki bölüm daha dikkat isteyecek."
            )
        }
    }
}

private fun buildPerformanceLabel(uiState: GameUiState): String {
    val accuracy = uiState.sessionSummary.accuracyPercent
    val combo = uiState.sessionSummary.bestCombo

    return when {
        accuracy >= 90 && combo >= 6 -> "Usta ritim"
        accuracy >= 80 -> "Temiz oyun"
        accuracy >= 65 -> "Dengeli oyun"
        accuracy >= 50 -> "Dalgalı oyun"
        else -> "Ham başlangıç"
    }
}

private data class CheckpointInfo(
    val unlockedNow: Boolean,
    val resumeLevel: Int,
    val levelsUntilNextCheckpoint: Int,
    val nextCheckpointUnlockLevel: Int
)
private data class GameOverCheckpointInfo(
    val hasCheckpoint: Boolean,
    val resumeLevel: Int,
    val savedAfterLevel: Int,
    val levelsCoveredSinceCheckpoint: Int
)

private fun buildGameOverCheckpointInfo(
    reachedLevel: Int,
    checkpointStartLevel: Int
): GameOverCheckpointInfo {
    val safeCheckpointStartLevel = checkpointStartLevel.coerceAtLeast(1)
    val hasCheckpoint = safeCheckpointStartLevel > 1
    val savedAfterLevel = (safeCheckpointStartLevel - 1).coerceAtLeast(0)

    val levelsCoveredSinceCheckpoint = if (hasCheckpoint) {
        (reachedLevel - savedAfterLevel).coerceAtLeast(0)
    } else {
        reachedLevel.coerceAtLeast(1)
    }

    return GameOverCheckpointInfo(
        hasCheckpoint = hasCheckpoint,
        resumeLevel = safeCheckpointStartLevel,
        savedAfterLevel = savedAfterLevel,
        levelsCoveredSinceCheckpoint = levelsCoveredSinceCheckpoint
    )
}
private fun buildCheckpointInfo(
    completedLevel: Int,
    savedCheckpointStartLevel: Int
): CheckpointInfo {
    val unlockedNow = completedLevel % 3 == 0

    val nextCheckpointUnlockLevel = if (unlockedNow) {
        completedLevel + 3
    } else {
        completedLevel + (3 - (completedLevel % 3))
    }

    val levelsUntilNextCheckpoint = (nextCheckpointUnlockLevel - completedLevel)
        .coerceAtLeast(0)

    val resumeLevel = if (unlockedNow) {
        completedLevel + 1
    } else {
        savedCheckpointStartLevel.coerceAtLeast(1)
    }

    return CheckpointInfo(
        unlockedNow = unlockedNow,
        resumeLevel = resumeLevel,
        levelsUntilNextCheckpoint = levelsUntilNextCheckpoint,
        nextCheckpointUnlockLevel = nextCheckpointUnlockLevel
    )
}
private fun calculateCheckpointStartLevelForLevel(level: Int): Int {
    val safeLevel = level.coerceAtLeast(1)

    if (safeLevel <= 3) return 1

    val checkpointBlockStart = ((safeLevel - 1) / 3) * 3 + 1
    return checkpointBlockStart
}

@Composable
private fun CountdownOverlay(
    onFinished: () -> Unit
) {
    var count by remember { mutableIntStateOf(3) }
    val scale = remember { Animatable(1.65f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        for (value in 3 downTo 1) {
            count = value

            scale.snapTo(1.65f)
            alpha.snapTo(0f)

            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 160,
                    easing = FastOutSlowInEasing
                )
            )

            scale.animateTo(
                targetValue = 0.92f,
                animationSpec = tween(
                    durationMillis = 690,
                    easing = FastOutSlowInEasing
                )
            )

            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 150,
                    easing = FastOutSlowInEasing
                )
            )
        }

        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(2.dp)
            .background(Color(0xFFFFFFF).copy(alpha = 0.10f))
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = 132.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
        )
    }
}
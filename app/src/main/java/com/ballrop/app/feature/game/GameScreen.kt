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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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

@Composable
fun GameScreen(
    startLevel: Int,
    onExitToMenu: () -> Unit
) {
    val context = LocalContext.current

    val progressStorage = remember { GameProgressStorage(context) }
    var checkpointStartLevel by remember { mutableIntStateOf(1) }

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
    }
    LaunchedEffect(uiState.status, uiState.currentLevel) {
        if (uiState.status == GameStatus.LEVEL_COMPLETED) {
            val completedLevel = uiState.currentLevel

            if (completedLevel % 3 == 0) {
                val nextCheckpointStartLevel = completedLevel + 1
                progressStorage.saveCheckpointStartLevel(nextCheckpointStartLevel)
            }
        }
    }

    LaunchedEffect(startLevel) {
        checkpointStartLevel = progressStorage.getCheckpointStartLevel()

        if (startLevel > 1) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BallropColors.BackgroundPrimary)
            .statusBarsPadding()
            .padding(horizontal = BallropDimens.Space16)
            .padding(top = BallropDimens.Space12, bottom = BallropDimens.Space12)
    ) {
        GameTopBar(
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
                .padding(top = BallropDimens.Space12)
        ) {
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

                when (uiState.status) {
                    GameStatus.LEVEL_COMPLETED -> {
                        LevelCompleteOverlay(
                            uiState = uiState,
                            onContinueClick = {
                                controller.onAction(GameAction.NextLevel)
                                controller.onAction(GameAction.StartGame)
                            },
                            onExitToMenuClick = onExitToMenu
                        )
                    }

                    GameStatus.PAUSED -> {
                        PauseOverlay(
                            onResumeClick = {
                                controller.onAction(GameAction.ResumeGame)
                            },
                            onRestartClick = {
                                controller.onAction(GameAction.ResetGame)
                                controller.onAction(GameAction.StartGame)
                            },
                            onExitToMenuClick = onExitToMenu
                        )
                    }

                    GameStatus.GAME_OVER -> {
                        GameOverOverlay(
                            uiState = uiState,
                            onRestartClick = {
                                val restartLevel = progressStorage.getCheckpointStartLevel()
                                    .coerceAtLeast(1)

                                controller.onAction(GameAction.StartFromLevel(restartLevel))
                                controller.onAction(GameAction.StartGame)
                            },
                            onExitToMenuClick = onExitToMenu
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun GameTopBar(
    uiState: GameUiState,
    onPauseClick: () -> Unit
) {
    val tempoLabel = when {
        uiState.levelProgress < uiState.levelConfig.introProgressThreshold -> "Rahat Başlangıç"
        uiState.levelProgress < uiState.levelConfig.rushProgressThreshold -> "Baskı Artıyor"
        else -> "Yoğun Tempo"
    }

    val remainingForLevel = (
            uiState.levelConfig.targetCorrectCount - uiState.correctPlacedCount
            ).coerceAtLeast(0)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(BallropDimens.Space12)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoCard(
                title = "Skor",
                value = uiState.score.toString(),
                modifier = Modifier.weight(1f)
            )

            InfoCard(
                title = "Level",
                value = uiState.currentLevel.toString(),
                modifier = Modifier.weight(1f)
            )

            InfoCard(
                title = "Hak",
                value = uiState.remainingLives.toString(),
                modifier = Modifier.weight(1f),
                highlighted = uiState.remainingLives <= 2
            )

            PauseButton(
                isPaused = uiState.status == GameStatus.PAUSED,
                enabled = uiState.status == GameStatus.PLAYING || uiState.status == GameStatus.PAUSED,
                modifier = Modifier.weight(1f),
                onClick = onPauseClick
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space12)
        ) {
            InfoCard(
                title = "Hedef",
                value = "${uiState.correctPlacedCount}/${uiState.levelConfig.targetCorrectCount}",
                modifier = Modifier.weight(1f)
            )

            InfoCard(
                title = "Combo",
                value = if (uiState.comboCount > 0) "x${uiState.comboCount}" else "-",
                modifier = Modifier.weight(1f),
                highlighted = uiState.comboCount >= 2
            )

            InfoCard(
                title = "En İyi",
                value = if (uiState.bestCombo > 0) "x${uiState.bestCombo}" else "-",
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Tempo: $tempoLabel",
            style = MaterialTheme.typography.labelLarge,
            color = BallropColors.TextSecondary,
            modifier = Modifier.padding(start = BallropDimens.Space4)
        )

        Text(
            text = "Bonus topu yakala, tehlikeli toptan kaçın.",
            style = MaterialTheme.typography.labelMedium,
            color = BallropColors.TextSecondary,
            modifier = Modifier.padding(start = BallropDimens.Space4)
        )

        Text(
            text = "Level için kalan: $remainingForLevel doğru",
            style = MaterialTheme.typography.labelMedium,
            color = BallropColors.TextSecondary,
            modifier = Modifier.padding(start = BallropDimens.Space4)
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
            Text(
                text = popup.text,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = if (popup.isCombo) {
                            popup.x - 88f
                        } else {
                            popup.x - 34f
                        }

                        translationY = popup.y
                        alpha = popup.alpha
                    }
                    .scale(popup.scale),
                style = if (popup.isCombo) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.titleLarge
                },
                textAlign = TextAlign.Center,
                color = when {
                    popup.isCombo -> BallropColors.Warning
                    popup.isPositive -> BallropColors.Success
                    else -> BallropColors.Error
                }
            )
        }
    }
}

@Composable
private fun LevelCompleteOverlay(
    uiState: GameUiState,
    onContinueClick: () -> Unit,
    onExitToMenuClick: () -> Unit
) {
    val tone = buildLevelCompleteTone(uiState)
    val nextLevel = uiState.currentLevel + 1
    val nextTarget = 20 + ((nextLevel - 1) * 2)

    CenterOverlayCard(
        title = tone.title,
        subtitle = tone.subtitle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space12)
        ) {
            SummaryCard(
                title = "Skor",
                value = uiState.score.toString(),
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Seri",
                value = "x${uiState.bestCombo}",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "İsabet",
                value = "%${uiState.accuracyPercent}",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space12)
        ) {
            SummaryCard(
                title = "Geçilen",
                value = "Level ${uiState.currentLevel}",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Sonraki",
                value = "Level $nextLevel",
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Yeni Hedef",
                value = "$nextTarget doğru",
                modifier = Modifier.weight(1f)
            )
        }

        SummaryCard(
            title = "Hazırlık",
            value = "Sonraki levelde tempo biraz daha sert olacak. Ritmi erken kur.",
            modifier = Modifier.fillMaxWidth()
        )

        OverlayButton(
            text = "Sonraki Level",
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
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitToMenuClick: () -> Unit
) {
    CenterOverlayCard(
        title = "OYUN DURAKLATILDI",
        subtitle = "İstersen devam et, istersen sıfırdan başla."
    ) {
        OverlayButton(
            text = "Devam Et",
            primary = true,
            onClick = onResumeClick
        )

        OverlayButton(
            text = "Yeniden Başlat",
            primary = false,
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
private fun GameOverOverlay(
    uiState: GameUiState,
    onRestartClick: () -> Unit,
    onExitToMenuClick: () -> Unit
) {
    val tone = buildGameOverTone(uiState)
    val performanceLabel = buildPerformanceLabel(uiState)

    CenterOverlayCard(
        title = tone.title,
        subtitle = tone.subtitle
    ) {
        SummaryCard(
            title = "Performans",
            value = performanceLabel,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space12)
        ) {
            SummaryCard(
                title = "Skor",
                value = uiState.sessionSummary.score.toString(),
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Level",
                value = uiState.sessionSummary.reachedLevel.toString(),
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "İsabet",
                value = "%${uiState.sessionSummary.accuracyPercent}",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BallropDimens.Space12)
        ) {
            SummaryCard(
                title = "Doğru",
                value = uiState.sessionSummary.totalCorrect.toString(),
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Hata",
                value = uiState.sessionSummary.totalMistakes.toString(),
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "En İyi Seri",
                value = "x${uiState.sessionSummary.bestCombo}",
                modifier = Modifier.weight(1f)
            )
        }

        SummaryCard(
            title = "Not",
            value = when {
                uiState.sessionSummary.accuracyPercent < 60 ->
                    "Önce temiz oyna, sonra hızlan. Checkpoint almadan risk büyür."

                uiState.sessionSummary.bestCombo < 3 ->
                    "Seri kurmaya odaklan. Asıl puan orada."

                else ->
                    "Ritmin fena değil. Checkpoint'e ulaşmak için 3 level bloklarını temiz geç."
            },
            modifier = Modifier.fillMaxWidth()
        )

        OverlayButton(
            text = "Tekrar Oyna",
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
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BallropColors.Black.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .background(
                    color = BallropColors.Surface.copy(alpha = 0.98f),
                    shape = RoundedCornerShape(BallropDimens.Radius24)
                )
                .padding(
                    horizontal = BallropDimens.Space24,
                    vertical = BallropDimens.Space24
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BallropDimens.Space12),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = BallropColors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BallropColors.TextSecondary,
                    textAlign = TextAlign.Center
                )

                content()
            }
        )
    }
}

@Composable
private fun OverlayButton(
    text: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (primary) {
                    BallropColors.BasketBlue
                } else {
                    BallropColors.SurfaceSoft
                },
                shape = RoundedCornerShape(BallropDimens.Radius16)
            )
            .clickable(onClick = onClick)
            .padding(vertical = BallropDimens.Space16),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = BallropColors.TextPrimary
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = BallropColors.SurfaceSoft,
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

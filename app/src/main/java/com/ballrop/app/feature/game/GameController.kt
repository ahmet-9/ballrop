package com.ballrop.app.feature.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ballrop.app.core.feedback.GameFeedbackEvent
import com.ballrop.app.core.feedback.GameFeedbackType
import com.ballrop.app.model.game.BallColor
import com.ballrop.app.model.game.BallItem
import com.ballrop.app.model.game.Basket
import com.ballrop.app.model.game.GameStatus
import com.ballrop.app.model.game.LevelConfig
import kotlin.math.hypot
import kotlin.random.Random
import com.ballrop.app.model.game.BallKind


class GameController {

    var uiState by mutableStateOf(GameUiState())
        private set

    private var nextBallId = 0L
    private var nextPopupId = 0L
    private var nextFeedbackEventId = 0L
    private var lastSpawnTimeMillis = 0L
    private var draggingBallId: Long? = null
    private var lastCorrectCatchMillis = 0L

    private var lastPointerX = 0f
    private var lastPointerY = 0f
    private var lastDragDeltaX = 0f
    private var lastDragDeltaY = 0f

    private var dragTouchOffsetX = 0f
    private var dragTouchOffsetY = 0f

    private var sessionStartTimeMillis = 0L
    private var levelStartTimeMillis = 0L


    fun onAction(action: GameAction) {
        when (action) {
            GameAction.StartGame -> {
                val now = System.currentTimeMillis()

                if (sessionStartTimeMillis == 0L) {
                    sessionStartTimeMillis = now
                }

                levelStartTimeMillis = now
                lastSpawnTimeMillis = now

                uiState = uiState.copy(
                    status = GameStatus.PLAYING,
                    levelConfig = currentLevelConfig(uiState.currentLevel)
                )
            }


            GameAction.ResetGame -> {
                resetSessionForLevel(level = 1)
            }


            GameAction.TryAgain -> {
                resetSessionForLevel(level = uiState.currentLevel)
            }





            GameAction.NextLevel -> {
                val nextLevel = uiState.currentLevel + 1
                nextBallId = 0L
                lastSpawnTimeMillis = System.currentTimeMillis()
                draggingBallId = null
                lastCorrectCatchMillis = 0L
                lastPointerX = 0f
                lastPointerY = 0f
                lastDragDeltaX = 0f
                lastDragDeltaY = 0f
                dragTouchOffsetX = 0f
                dragTouchOffsetY = 0f

                uiState = uiState.copy(
                    currentLevel = nextLevel,
                    correctPlacedCount = 0,
                    comboCount = 0,
                    balls = emptyList(),
                    feedbackPopups = emptyList(),
                    feedbackEvents = emptyList(),
                    baskets = uiState.baskets.map { it.copy(glowAlpha = 0f) },
                    status = GameStatus.PLAYING,
                    levelConfig = currentLevelConfig(nextLevel)
                )
            }

            GameAction.PauseGame -> {
                if (uiState.status == GameStatus.PLAYING) {
                    draggingBallId = null
                    dragTouchOffsetX = 0f
                    dragTouchOffsetY = 0f

                    uiState = uiState.copy(
                        balls = uiState.balls.map {
                            it.copy(
                                isDragging = false
                            )
                        },
                        status = GameStatus.PAUSED
                    )
                }
            }

            GameAction.ResumeGame -> {
                if (uiState.status == GameStatus.PAUSED) {
                    lastSpawnTimeMillis = System.currentTimeMillis()
                    uiState = uiState.copy(status = GameStatus.PLAYING)
                }
            }

            is GameAction.UpdateCanvasSize -> {
                val baskets = createBaskets(
                    width = action.width,
                    height = action.height
                )

                uiState = uiState.copy(
                    canvasWidth = action.width,
                    canvasHeight = action.height,
                    baskets = baskets
                )
            }

            is GameAction.PointerDown -> handlePointerDown(action.x, action.y)
            is GameAction.PointerMove -> handlePointerMove(action.x, action.y)
            GameAction.PointerUp -> handlePointerUp()

            is GameAction.ConsumeFeedback -> {
                uiState = uiState.copy(
                    feedbackEvents = uiState.feedbackEvents.filterNot { it.id == action.id }
                )
            }

            GameAction.Tick -> {
                updateFeedbackPopups()
                updateBasketGlow()

                if (uiState.status != GameStatus.PLAYING) return
                if (uiState.canvasWidth <= 0f || uiState.canvasHeight <= 0f) return

                spawnBallIfNeeded()
                updateFallingBalls()
                resolveBasketHits()
                removeMissedBalls()
                checkGameOver()
            }
            is GameAction.StartFromLevel -> {
                resetSessionForLevel(level = action.level)
            }

        }
    }

    private fun handlePointerDown(x: Float, y: Float) {
        if (uiState.status != GameStatus.PLAYING) return
        if (uiState.balls.isEmpty()) return

        val touchedBall = uiState.balls
            .asReversed()
            .firstOrNull { ball -> isPointInsideBall(x, y, ball) }

        if (touchedBall == null) {
            draggingBallId = null
            dragTouchOffsetX = 0f
            dragTouchOffsetY = 0f
            return
        }

        draggingBallId = touchedBall.id
        lastPointerX = x
        lastPointerY = y
        lastDragDeltaX = 0f
        lastDragDeltaY = 0f

        dragTouchOffsetX = touchedBall.x - x
        dragTouchOffsetY = touchedBall.y - y

        uiState = uiState.copy(
            balls = uiState.balls.map { ball ->
                if (ball.id == touchedBall.id) {
                    ball.copy(
                        isDragging = true,
                        velocityX = 0f,
                        velocityY = 0f,
                        scale = 1.07f
                    )
                } else {
                    ball.copy(isDragging = false)
                }
            }
        )
    }

    private fun handlePointerMove(x: Float, y: Float) {
        val targetId = draggingBallId ?: return
        if (uiState.status != GameStatus.PLAYING) return

        lastDragDeltaX = x - lastPointerX
        lastDragDeltaY = y - lastPointerY
        lastPointerX = x
        lastPointerY = y

        uiState = uiState.copy(
            balls = uiState.balls.map { ball ->
                if (ball.id == targetId) {
                    val targetCenterX = x + dragTouchOffsetX
                    val targetCenterY = y + dragTouchOffsetY

                    val clampedX = targetCenterX.coerceIn(
                        ball.radius,
                        uiState.canvasWidth - ball.radius
                    )
                    val clampedY = targetCenterY.coerceIn(
                        ball.radius,
                        uiState.canvasHeight - ball.radius
                    )

                    ball.copy(
                        x = clampedX,
                        y = clampedY,
                        isDragging = true,
                        rotationDeg = ball.rotationDeg + 10f,
                        scale = 1.07f
                    )
                } else {
                    ball
                }
            }
        )
    }

    private fun handlePointerUp() {
        val targetId = draggingBallId
        draggingBallId = null

        if (targetId == null) return

        val releaseVelocityX = (lastDragDeltaX * 0.55f).coerceIn(-18f, 18f)
        val releaseVelocityY = (lastDragDeltaY * 0.55f).coerceIn(-18f, 18f)

        uiState = uiState.copy(
            balls = uiState.balls.map { ball ->
                if (ball.id == targetId) {
                    ball.copy(
                        isDragging = false,
                        velocityX = releaseVelocityX,
                        velocityY = releaseVelocityY,
                        scale = 1f
                    )
                } else {
                    ball
                }
            }
        )

        lastDragDeltaX = 0f
        lastDragDeltaY = 0f
        dragTouchOffsetX = 0f
        dragTouchOffsetY = 0f

        resolveBasketHits()
        removeMissedBalls()
        checkGameOver()
    }

    private fun spawnBallIfNeeded() {
        val now = System.currentTimeMillis()
        val interval = currentSpawnIntervalMillis()

        if (now - lastSpawnTimeMillis < interval) return

        val radius = (uiState.canvasWidth * 0.045f).coerceIn(18f, 32f)
        val minX = radius
        val maxX = uiState.canvasWidth - radius

        if (maxX <= minX) return

        val randomX = Random.nextFloat() * (maxX - minX) + minX
        val color = if (Random.nextBoolean()) BallColor.BLUE else BallColor.RED
        val kind = chooseBallKind()


        val drift = (
                (Random.nextFloat() * 2f - 1f) *
                        uiState.levelConfig.horizontalDriftMultiplier *
                        currentDriftMultiplier()
                )

        val rotationSpeed = Random.nextFloat() * 7f + 2f
        val signedRotationSpeed = if (Random.nextBoolean()) rotationSpeed else -rotationSpeed

        val newBall = BallItem(
            id = nextBallId++,
            color = color,
            kind = kind,
            x = randomX,
            y = radius + 8f,
            radius = radius,
            speed = uiState.levelConfig.baseFallSpeed / 60f,
            driftX = drift,
            velocityX = 0f,
            velocityY = 0f,
            rotationDeg = Random.nextFloat() * 360f,
            rotationSpeedDeg = signedRotationSpeed,
            scale = 1f
        )


        uiState = uiState.copy(
            balls = uiState.balls + newBall
        )

        lastSpawnTimeMillis = now
    }


    private fun updateFallingBalls() {
        val tempoFallMultiplier = currentFallMultiplier()

        uiState = uiState.copy(
            balls = uiState.balls.map { ball ->
                if (ball.isDragging) {
                    ball
                } else {
                    val nextVelocityX = ball.velocityX * 0.90f
                    val nextVelocityY = ball.velocityY * 0.90f

                    val nextX = (
                            ball.x +
                                    (ball.driftX * tempoFallMultiplier) +
                                    nextVelocityX
                            ).coerceIn(ball.radius, uiState.canvasWidth - ball.radius)

                    val nextY = ball.y + (ball.speed * tempoFallMultiplier) + nextVelocityY

                    val nextScale = when {
                        ball.justCaptured -> (ball.scale - 0.08f).coerceAtLeast(0.76f)
                        ball.scale < 1f -> (ball.scale + 0.05f).coerceAtMost(1f)
                        ball.scale > 1f -> (ball.scale - 0.04f).coerceAtLeast(1f)
                        else -> 1f
                    }

                    ball.copy(
                        x = nextX,
                        y = nextY,
                        velocityX = nextVelocityX,
                        velocityY = nextVelocityY,
                        rotationDeg = ball.rotationDeg + ball.rotationSpeedDeg,
                        scale = nextScale,
                        justCaptured = false
                    )
                }
            }
        )
    }


    private fun resolveBasketHits() {
        if (uiState.balls.isEmpty() || uiState.baskets.isEmpty()) return

        val now = System.currentTimeMillis()
        val caughtBallIds = mutableSetOf<Long>()
        val newPopups = mutableListOf<GameFeedbackPopup>()
        val newFeedbackEvents = mutableListOf<GameFeedbackEvent>()
        val hitBasketColors = mutableSetOf<BallColor>()

        var scoreDelta = 0
        var correctDelta = 0
        var wrongDelta = 0
        var comboCount = uiState.comboCount
        var bestCombo = uiState.bestCombo

        uiState.balls.forEach { ball ->
            if (ball.isDragging) return@forEach

            val hitBasket = uiState.baskets.firstOrNull { basket ->
                isBallInsideBasket(ball, basket)
            } ?: return@forEach

            caughtBallIds += ball.id
            hitBasketColors += hitBasket.color

            when (ball.kind) {
                BallKind.DANGER -> {
                    scoreDelta -= uiState.levelConfig.dangerPenalty
                    wrongDelta += 1
                    comboCount = 0
                    lastCorrectCatchMillis = 0L

                    newPopups += createPopup(
                        text = "TEHLİKE -${uiState.levelConfig.dangerPenalty}",
                        x = ball.x,
                        y = ball.y - (ball.radius * 1.35f),
                        isPositive = false,
                        isCombo = true
                    )

                    newFeedbackEvents += createFeedbackEvent(GameFeedbackType.WRONG)
                }

                BallKind.NORMAL,
                BallKind.BONUS -> {
                    if (ball.color == hitBasket.color) {
                        comboCount = if (
                            comboCount > 0 &&
                            now - lastCorrectCatchMillis <= uiState.levelConfig.comboResetWindowMillis
                        ) {
                            comboCount + 1
                        } else {
                            1
                        }

                        lastCorrectCatchMillis = now
                        bestCombo = maxOf(bestCombo, comboCount)

                        val comboBonusSteps = (comboCount - 1)
                            .coerceAtLeast(0)
                            .coerceAtMost(uiState.levelConfig.maxComboBonusSteps)

                        val basePoints = uiState.levelConfig.correctBaseScore + (comboBonusSteps * 5)
                        val extraBonus = if (ball.kind == BallKind.BONUS) {
                            uiState.levelConfig.bonusExtraScore
                        } else {
                            0
                        }

                        val totalPoints = basePoints + extraBonus

                        scoreDelta += totalPoints
                        correctDelta += 1

                        newPopups += createPopup(
                            text = if (ball.kind == BallKind.BONUS) {
                                "BONUS +$totalPoints"
                            } else {
                                "+$totalPoints"
                            },
                            x = ball.x,
                            y = ball.y - (ball.radius * 1.35f),
                            isPositive = true,
                            isCombo = ball.kind == BallKind.BONUS
                        )

                        if (comboCount >= 2) {
                            newPopups += createPopup(
                                text = "COMBO x$comboCount",
                                x = uiState.canvasWidth / 2f,
                                y = (uiState.canvasHeight * 0.26f).coerceAtLeast(90f),
                                isPositive = true,
                                isCombo = true
                            )
                        }

                        newFeedbackEvents += createFeedbackEvent(GameFeedbackType.CORRECT)
                    } else {
                        scoreDelta -= uiState.levelConfig.wrongPenalty
                        wrongDelta += 1
                        comboCount = 0
                        lastCorrectCatchMillis = 0L

                        newPopups += createPopup(
                            text = "-${uiState.levelConfig.wrongPenalty}",
                            x = ball.x,
                            y = ball.y - (ball.radius * 1.3f),
                            isPositive = false,
                            isCombo = false
                        )

                        newFeedbackEvents += createFeedbackEvent(GameFeedbackType.WRONG)
                    }
                }
            }
        }

        if (caughtBallIds.isEmpty()) return

        val newCorrectCount = uiState.correctPlacedCount + correctDelta
        val target = uiState.levelConfig.targetCorrectCount
        val levelCompleted = newCorrectCount >= target

        uiState = uiState.copy(
            balls = uiState.balls.filterNot { it.id in caughtBallIds },
            score = (uiState.score + scoreDelta).coerceAtLeast(0),
            correctPlacedCount = newCorrectCount,
            totalCorrectCount = uiState.totalCorrectCount + correctDelta,
            wrongCount = uiState.wrongCount + wrongDelta,
            comboCount = comboCount,
            bestCombo = bestCombo,
            feedbackPopups = uiState.feedbackPopups + newPopups,
            feedbackEvents = uiState.feedbackEvents + newFeedbackEvents +
                    if (levelCompleted) {
                        listOf(createFeedbackEvent(GameFeedbackType.LEVEL_COMPLETE))
                    } else {
                        emptyList()
                    },
            baskets = uiState.baskets.map { basket ->
                if (basket.color in hitBasketColors) {
                    basket.copy(glowAlpha = 1f)
                } else {
                    basket
                }
            },
            status = if (levelCompleted) GameStatus.LEVEL_COMPLETED else uiState.status
        )
    }


    private fun removeMissedBalls() {
        if (uiState.balls.isEmpty()) return

        val missedBalls = uiState.balls.filter { ball ->
            ball.y - ball.radius > uiState.canvasHeight
        }

        if (missedBalls.isEmpty()) return

        val missedIds = missedBalls.map { it.id }.toSet()
        val remainingBalls = uiState.balls.filterNot { ball -> ball.id in missedIds }

        val normalMissed = missedBalls.filter { it.kind != BallKind.DANGER }
        val escapedDangerBalls = missedBalls.filter { it.kind == BallKind.DANGER }

        val missPopups = mutableListOf<GameFeedbackPopup>()

        normalMissed.forEach { ball ->
            missPopups += createPopup(
                text = "-${uiState.levelConfig.missPenalty}",
                x = ball.x,
                y = (uiState.canvasHeight - 36f).coerceAtLeast(24f),
                isPositive = false,
                isCombo = false
            )
        }

        escapedDangerBalls.forEach { ball ->
            missPopups += createPopup(
                text = "KAÇINMA +${uiState.levelConfig.dangerEscapeScore}",
                x = ball.x,
                y = (uiState.canvasHeight - 36f).coerceAtLeast(24f),
                isPositive = true,
                isCombo = true
            )
        }

        val normalMissCount = normalMissed.size
        val escapedDangerCount = escapedDangerBalls.size

        val scoreAfterPenalty = uiState.score - (normalMissCount * uiState.levelConfig.missPenalty)
        val scoreAfterDangerReward = scoreAfterPenalty + (escapedDangerCount * uiState.levelConfig.dangerEscapeScore)

        if (normalMissCount > 0) {
            lastCorrectCatchMillis = 0L
        }

        uiState = uiState.copy(
            balls = remainingBalls,
            missedCount = uiState.missedCount + normalMissCount,
            score = scoreAfterDangerReward.coerceAtLeast(0),
            comboCount = if (normalMissCount > 0) 0 else uiState.comboCount,
            feedbackPopups = uiState.feedbackPopups + missPopups,
            feedbackEvents = uiState.feedbackEvents +
                    when {
                        normalMissCount > 0 && escapedDangerCount > 0 -> {
                            listOf(
                                createFeedbackEvent(GameFeedbackType.MISS),
                                createFeedbackEvent(GameFeedbackType.CORRECT)
                            )
                        }

                        normalMissCount > 0 -> {
                            listOf(createFeedbackEvent(GameFeedbackType.MISS))
                        }

                        escapedDangerCount > 0 -> {
                            listOf(createFeedbackEvent(GameFeedbackType.CORRECT))
                        }

                        else -> emptyList()
                    }
        )
    }


    private fun updateFeedbackPopups() {
        if (uiState.feedbackPopups.isEmpty()) return

        uiState = uiState.copy(
            feedbackPopups = uiState.feedbackPopups
                .map { popup ->
                    if (popup.isCombo) {
                        popup.copy(
                            y = popup.y - 1.6f,
                            alpha = (popup.alpha - 0.018f).coerceAtLeast(0f),
                            scale = (popup.scale + 0.020f).coerceAtMost(1.18f)
                        )
                    } else {
                        popup.copy(
                            y = popup.y - 2.6f,
                            alpha = (popup.alpha - 0.030f).coerceAtLeast(0f),
                            scale = (popup.scale + 0.014f).coerceAtMost(1.08f)
                        )
                    }
                }
                .filter { it.alpha > 0.05f }
        )
    }

    private fun updateBasketGlow() {
        if (uiState.baskets.isEmpty()) return

        uiState = uiState.copy(
            baskets = uiState.baskets.map { basket ->
                basket.copy(
                    glowAlpha = (basket.glowAlpha - 0.06f).coerceAtLeast(0f)
                )
            }
        )
    }

    private fun checkGameOver() {
        if (uiState.status == GameStatus.LEVEL_COMPLETED) return
        if (uiState.status == GameStatus.GAME_OVER) return

        if (uiState.totalMistakes >= uiState.maxMistakesAllowed) {
            val durationSeconds = if (sessionStartTimeMillis == 0L) {
                0
            } else {
                ((System.currentTimeMillis() - sessionStartTimeMillis) / 1000L).toInt()
            }

            draggingBallId = null
            lastCorrectCatchMillis = 0L
            dragTouchOffsetX = 0f
            dragTouchOffsetY = 0f

            uiState = uiState.copy(
                balls = emptyList(),
                comboCount = 0,
                status = GameStatus.GAME_OVER,
                sessionSummary = com.ballrop.app.model.game.GameSessionSummary(
                    score = uiState.score,
                    reachedLevel = uiState.currentLevel,
                    totalCorrect = uiState.totalCorrectCount,
                    totalMistakes = uiState.totalMistakes,
                    bestCombo = uiState.bestCombo,
                    accuracyPercent = uiState.accuracyPercent,
                    durationSeconds = durationSeconds
                ),
                feedbackEvents = uiState.feedbackEvents + createFeedbackEvent(GameFeedbackType.GAME_OVER)
            )
        }
    }


    private fun createPopup(
        text: String,
        x: Float,
        y: Float,
        isPositive: Boolean,
        isCombo: Boolean = false
    ): GameFeedbackPopup {
        return GameFeedbackPopup(
            id = nextPopupId++,
            text = text,
            x = x,
            y = y,
            isPositive = isPositive,
            isCombo = isCombo,
            scale = if (isCombo) 0.96f else 0.88f
        )
    }

    private fun createFeedbackEvent(
        type: GameFeedbackType
    ): GameFeedbackEvent {
        return GameFeedbackEvent(
            id = nextFeedbackEventId++,
            type = type
        )
    }

    private fun isPointInsideBall(
        x: Float,
        y: Float,
        ball: BallItem
    ): Boolean {
        val distance = hypot(x - ball.x, y - ball.y)
        return distance <= ball.radius
    }

    private fun isBallInsideBasket(
        ball: BallItem,
        basket: Basket
    ): Boolean {
        val basketWidth = basket.right - basket.left
        val basketHeight = basket.bottom - basket.top

        val innerLeft = basket.left + (basketWidth * 0.12f)
        val innerRight = basket.right - (basketWidth * 0.12f)

        val entryTop = basket.top - (ball.radius * 0.35f)
        val entryBottom = basket.top + (basketHeight * 0.32f)

        val centerInsideEntryWidth =
            ball.x >= innerLeft && ball.x <= innerRight

        val ballBottom = ball.y + ball.radius
        val crossedEntryBand =
            ballBottom >= entryTop && ballBottom <= entryBottom

        return centerInsideEntryWidth && crossedEntryBand
    }

    private fun currentSpawnIntervalMillis(): Long {
        val config = uiState.levelConfig
        val base = config.spawnIntervalMillis.toFloat()

        val value = when {
            uiState.levelProgress < config.introProgressThreshold -> {
                base * config.introSpawnMultiplier
            }

            uiState.levelProgress < config.rushProgressThreshold -> {
                base
            }

            else -> {
                base * config.rushSpawnMultiplier
            }
        }

        return value.toLong().coerceAtLeast(260L)
    }

    private fun currentFallMultiplier(): Float {
        val config = uiState.levelConfig

        return when {
            uiState.levelProgress < config.introProgressThreshold -> {
                config.introFallMultiplier
            }

            uiState.levelProgress < config.rushProgressThreshold -> {
                1f
            }

            else -> {
                config.rushFallMultiplier
            }
        }
    }

    private fun currentDriftMultiplier(): Float {
        val config = uiState.levelConfig

        return when {
            uiState.levelProgress < config.introProgressThreshold -> 1f
            uiState.levelProgress < config.rushProgressThreshold -> 1.05f
            else -> config.rushDriftMultiplier
        }
    }
    private fun chooseBallKind(): BallKind {
        val roll = Random.nextFloat()
        val config = uiState.levelConfig

        return when {
            roll < config.dangerBallChance -> BallKind.DANGER
            roll < config.dangerBallChance + config.bonusBallChance -> BallKind.BONUS
            else -> BallKind.NORMAL
        }
    }



    private fun currentLevelConfig(level: Int): LevelConfig {
        val safeLevel = level.coerceAtLeast(1)

        val targetCorrectCount = (20 + ((safeLevel - 1) * 2)).coerceAtMost(42)
        val spawnIntervalMillis = (1150L - ((safeLevel - 1) * 90L)).coerceAtLeast(520L)
        val baseFallSpeed = 240f + ((safeLevel - 1) * 35f)
        val comboResetWindowMillis = (1800L - ((safeLevel - 1) * 120L)).coerceAtLeast(900L)
        val maxComboBonusSteps = (2 + ((safeLevel - 1) / 2)).coerceAtMost(5)
        val horizontalDriftMultiplier = 1f + ((safeLevel - 1) * 0.10f)

        val bonusBallChance = when {
            safeLevel <= 1 -> 0f
            safeLevel <= 3 -> 0.08f
            safeLevel <= 6 -> 0.12f
            else -> 0.16f
        }

        val dangerBallChance = when {
            safeLevel <= 3 -> 0f
            safeLevel <= 6 -> 0.06f
            safeLevel <= 10 -> 0.10f
            else -> 0.14f
        }

        return LevelConfig(
            targetCorrectCount = targetCorrectCount,
            spawnIntervalMillis = spawnIntervalMillis,
            baseFallSpeed = baseFallSpeed,
            comboResetWindowMillis = comboResetWindowMillis,
            maxComboBonusSteps = maxComboBonusSteps,
            horizontalDriftMultiplier = horizontalDriftMultiplier,
            correctBaseScore = 10,
            wrongPenalty = 5,
            missPenalty = 5,

            introProgressThreshold = 0.30f,
            rushProgressThreshold = 0.75f,

            introSpawnMultiplier = 1.18f,
            introFallMultiplier = 0.88f,

            rushSpawnMultiplier = 0.78f,
            rushFallMultiplier = 1.22f,
            rushDriftMultiplier = 1.18f,

            bonusBallChance = bonusBallChance,
            dangerBallChance = dangerBallChance,
            bonusExtraScore = 15,
            dangerPenalty = 12,
            dangerEscapeScore = 8
        )
    }

    private fun resetSessionForLevel(level: Int) {
        val preservedWidth = uiState.canvasWidth
        val preservedHeight = uiState.canvasHeight
        val safeLevel = level.coerceAtLeast(1)

        val preservedBaskets = if (preservedWidth > 0f && preservedHeight > 0f) {
            createBaskets(
                width = preservedWidth,
                height = preservedHeight
            )
        } else {
            emptyList()
        }

        nextBallId = 0L
        nextPopupId = 0L
        nextFeedbackEventId = 0L
        lastSpawnTimeMillis = 0L
        draggingBallId = null
        lastCorrectCatchMillis = 0L
        lastPointerX = 0f
        lastPointerY = 0f
        lastDragDeltaX = 0f
        lastDragDeltaY = 0f
        dragTouchOffsetX = 0f
        dragTouchOffsetY = 0f
        sessionStartTimeMillis = 0L
        levelStartTimeMillis = 0L

        uiState = GameUiState(
            currentLevel = safeLevel,
            levelConfig = currentLevelConfig(safeLevel),
            canvasWidth = preservedWidth,
            canvasHeight = preservedHeight,
            baskets = preservedBaskets
        )
    }


    private fun createBaskets(width: Float, height: Float): List<Basket> {
        val isTabletLike = width >= 900f

        val horizontalPadding = if (isTabletLike) width * 0.14f else width * 0.08f
        val basketSpacing = if (isTabletLike) width * 0.08f else width * 0.06f
        val basketWidth = (width - (horizontalPadding * 2) - basketSpacing) / 2f
        val basketHeight = if (isTabletLike) height * 0.10f else height * 0.12f

        val bottomMargin = if (isTabletLike) height * 0.16f else height * 0.14f
        val basketTop = height - basketHeight - bottomMargin

        val leftBasketX = horizontalPadding
        val rightBasketX = leftBasketX + basketWidth + basketSpacing

        val blueBasket = Basket(
            color = BallColor.BLUE,
            left = leftBasketX,
            top = basketTop,
            right = leftBasketX + basketWidth,
            bottom = basketTop + basketHeight
        )

        val redBasket = Basket(
            color = BallColor.RED,
            left = rightBasketX,
            top = basketTop,
            right = rightBasketX + basketWidth,
            bottom = basketTop + basketHeight
        )

        return listOf(blueBasket, redBasket)
    }
}

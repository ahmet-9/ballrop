package com.ballrop.app.feature.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ballrop.app.core.feedback.GameFeedbackEvent
import com.ballrop.app.core.feedback.GameFeedbackType
import com.ballrop.app.engine.spawn.DifficultyResolver
import com.ballrop.app.feature.game.flow.GameFlowDirective
import com.ballrop.app.feature.game.flow.GameFlowEngine
import com.ballrop.app.feature.game.shape.ShapeNode
import com.ballrop.app.feature.game.shape.ShapePatternFactory
import com.ballrop.app.model.game.BallColor
import com.ballrop.app.model.game.BallItem
import com.ballrop.app.model.game.BallKind
import com.ballrop.app.model.game.BallMovementType
import com.ballrop.app.model.game.Basket
import com.ballrop.app.model.game.BasketMotionMode
import com.ballrop.app.model.game.GameSessionSummary
import com.ballrop.app.model.game.GameStatus
import com.ballrop.app.model.game.LevelConfig
import com.ballrop.app.model.game.LevelPartConfig
import com.ballrop.app.model.game.SpawnMixEntry
import com.ballrop.app.model.game.SpawnPatternType
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

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

    private var slowMotionUntilMillis = 0L
    private var flowEngine: GameFlowEngine? = null

    private var basketLeftLane: Basket? = null
    private var basketRightLane: Basket? = null
    private var basketSwayOffset = 0f
    private var basketSwayDirection = 1f
    private var basketSwapState = false
    private var lastSwapThreshold = 0

    fun onAction(action: GameAction) {
        when (action) {
            GameAction.StartGame -> {
                val now = System.currentTimeMillis()
                val levelConfig = currentLevelConfig(uiState.currentLevel)
                val partConfig = levelConfig.partOrFirst(uiState.currentPart)

                if (sessionStartTimeMillis == 0L) {
                    sessionStartTimeMillis = now
                }

                levelStartTimeMillis = now
                lastSpawnTimeMillis = now
                flowEngine = GameFlowEngine(partConfig)

                uiState = uiState.copy(
                    status = GameStatus.PLAYING,
                    levelConfig = levelConfig,
                    maxMistakesAllowed = partConfig.maxMistakesAllowed,
                    baskets = snapBasketsToMotionStart(uiState.baskets)
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
                resetSessionForLevel(level = nextLevel)
                val now = System.currentTimeMillis()
                sessionStartTimeMillis = now
                levelStartTimeMillis = now
                lastSpawnTimeMillis = now
                flowEngine = GameFlowEngine(uiState.levelConfig.partOrFirst(1))

                uiState = uiState.copy(
                    status = GameStatus.PLAYING,
                    baskets = snapBasketsToMotionStart(uiState.baskets)
                )
            }

            GameAction.PauseGame -> {
                if (uiState.status == GameStatus.PLAYING) {
                    draggingBallId = null
                    dragTouchOffsetX = 0f
                    dragTouchOffsetY = 0f

                    uiState = uiState.copy(
                        balls = uiState.balls.map {
                            it.copy(isDragging = false)
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
                    height = action.height,
                    colorCount = activePartConfig().colorCount
                )

                basketLeftLane = baskets.getOrNull(0)
                basketRightLane = baskets.getOrNull(1)

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
                updateImpactOverlay()

                if (uiState.status != GameStatus.PLAYING) return
                if (uiState.canvasWidth <= 0f || uiState.canvasHeight <= 0f) return

                updateBasketMotion()
                spawnBallIfNeeded()
                updateFallingBalls()
                resolveBasketHits()
                removeMissedBalls()
                checkGameOver()
            }

            is GameAction.StartFromLevel -> {
                resetSessionForLevel(level = action.level)
            }

            GameAction.DebugAddLives -> {
                uiState = uiState.copy(
                    maxMistakesAllowed = uiState.maxMistakesAllowed + 10
                )
            }

            GameAction.DebugAddCorrectCatch -> {
                val updatedCorrectCount =
                    (uiState.correctPlacedCount + 1)
                        .coerceAtMost(uiState.targetCorrectCountForActivePart)

                uiState = uiState.copy(
                    correctPlacedCount = updatedCorrectCount,
                    totalCorrectCount = uiState.totalCorrectCount + 1,
                    score = uiState.score + 1
                )

                if (updatedCorrectCount >= uiState.targetCorrectCountForActivePart) {
                    handlePartCompleted()
                }
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
        val engine = ensureFlowEngine()

        when (val directive = engine.update(now)) {
            is GameFlowDirective.SpawnShape -> {
                spawnShapeBlock(directive.shapeCode)
                return
            }

            GameFlowDirective.Waiting -> {
                return
            }

            GameFlowDirective.Completed -> {
                return
            }

            is GameFlowDirective.BallEntriesReady -> {
                val interval = activePartConfig().spawnIntervalMillis
                if (now - lastSpawnTimeMillis < interval) return

                val radius = (uiState.canvasWidth * 0.052f).coerceIn(26f, 48f)
                val minX = radius
                val maxX = uiState.canvasWidth - radius

                if (maxX <= minX) return

                val spec = chooseSpawnSpec(directive.entries)

                val movementType = spec.movementType

                val baseDrift = when (movementType) {
                    BallMovementType.STRAIGHT -> 0f
                    BallMovementType.DRIFT_LEFT -> -(0.8f + Random.nextFloat() * 0.6f)
                    BallMovementType.DRIFT_RIGHT -> 0.8f + Random.nextFloat() * 0.6f
                    BallMovementType.ZIGZAG_SOFT -> 0f
                    BallMovementType.ZIGZAG_HARD -> 0f
                    BallMovementType.DIAGONAL_LEFT -> -activePartBaseDriftStrength()
                    BallMovementType.DIAGONAL_RIGHT -> activePartBaseDriftStrength()
                }

                val rotationSpeed = Random.nextFloat() * 7f + 2f
                val signedRotationSpeed = if (Random.nextBoolean()) rotationSpeed else -rotationSpeed

                val targetColor = randomBallColor()
                val initialColor = if (spec.kind == BallKind.COLOR_SHIFT) {
                    randomDifferentColor(targetColor)
                } else {
                    targetColor
                }

                val spawnX = Random.nextFloat() * (maxX - minX) + minX
                val spawnY = radius + 8f

                val newBall = BallItem(
                    id = nextBallId++,
                    color = initialColor,
                    targetColor = targetColor,
                    kind = spec.kind,
                    spawnCode = spec.spawnCode,
                    x = spawnX,
                    y = spawnY,
                    radius = radius,
                    speed = (activePartBaseFallSpeed() * spec.speedMultiplier) / 60f,
                    driftX = baseDrift,
                    velocityX = 0f,
                    velocityY = 0f,
                    rotationDeg = Random.nextFloat() * 360f,
                    rotationSpeedDeg = signedRotationSpeed,
                    scale = 1f,
                    movementType = movementType,
                    movementPhase = Random.nextFloat() * 6.2831855f,
                    lockedToPattern = false,
                    colorRevealLocked = spec.kind != BallKind.COLOR_SHIFT
                )

                uiState = uiState.copy(
                    balls = uiState.balls + newBall
                )

                lastSpawnTimeMillis = now
                engine.onBallSpawned()
            }
        }
    }

    private fun spawnShapeBlock(shapeCode: Int) {
        val radius = (uiState.canvasWidth * 0.065f).coerceIn(28f, 52f)
        val spacing = radius * 2.6f

        val nodes = ShapePatternFactory.create(shapeCode)
        if (nodes.isEmpty()) return

        val minGridX = nodes.minOf { it.gridX }
        val maxGridX = nodes.maxOf { it.gridX }
        val minGridY = nodes.minOf { it.gridY }
        val maxGridY = nodes.maxOf { it.gridY }

        val normalizedWidthCells = (maxGridX - minGridX) + 1
        val normalizedHeightCells = (maxGridY - minGridY) + 1

        val shapeWidth = ((normalizedWidthCells - 1) * spacing) + (radius * 2f)
        val shapeHeight = ((normalizedHeightCells - 1) * spacing) + (radius * 2f)

        val startX = ((uiState.canvasWidth - shapeWidth) / 2f).coerceAtLeast(radius)
        val startY = -shapeHeight - radius

        val shapeBalls = nodes.map { node ->
            createShapeBall(
                node = node,
                minGridX = minGridX,
                minGridY = minGridY,
                startX = startX,
                startY = startY,
                radius = radius,
                spacing = spacing
            )
        }

        val ids = shapeBalls.map { it.id }.toSet()

        uiState = uiState.copy(
            balls = uiState.balls + shapeBalls,
            activeShapeBallIds = ids,
            shapeRemainingCount = ids.size,
            shapeTotalCount = ids.size,
            isShapeActive = ids.isNotEmpty()
        )
    }

    private fun createShapeBall(
        node: ShapeNode,
        minGridX: Int,
        minGridY: Int,
        startX: Float,
        startY: Float,
        radius: Float,
        spacing: Float
    ): BallItem {
        val normalizedGridX = node.gridX - minGridX
        val normalizedGridY = node.gridY - minGridY

        val x = startX + (normalizedGridX * spacing) + radius
        val y = startY + (normalizedGridY * spacing) + radius

        val shapeSpec = chooseShapeSpawnSpec()
        val targetColor = randomBallColor()
        val initialColor = if (shapeSpec.kind == BallKind.COLOR_SHIFT) {
            randomDifferentColor(targetColor)
        } else {
            targetColor
        }

        return BallItem(
            id = nextBallId++,
            color = initialColor,
            targetColor = targetColor,
            kind = shapeSpec.kind,
            spawnCode = shapeSpec.spawnCode,
            x = x.coerceIn(radius, uiState.canvasWidth - radius),
            y = y,
            radius = radius,
            speed = (activePartBaseFallSpeed() * shapeSpec.speedMultiplier) / 60f,
            driftX = 0f,
            velocityX = 0f,
            velocityY = 0f,
            rotationDeg = Random.nextFloat() * 360f,
            rotationSpeedDeg = if (shapeSpec.kind == BallKind.BOMB) 1.2f else 0.8f,
            scale = 1f,
            movementType = BallMovementType.STRAIGHT,
            movementPhase = 0f,
            lockedToPattern = true,
            colorRevealLocked = shapeSpec.kind != BallKind.COLOR_SHIFT
        )
    }

    private fun chooseShapeSpawnSpec(): SpawnSpec {
        val roll = Random.nextInt(100)

        return when {
            roll < 72 -> SpawnSpec(
                spawnCode = 1,
                kind = BallKind.NORMAL,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 1f
            )

            roll < 82 -> SpawnSpec(
                spawnCode = 2,
                kind = BallKind.BONUS,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 1f
            )

            roll < 91 -> SpawnSpec(
                spawnCode = 3,
                kind = BallKind.BOMB,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 1f
            )

            else -> SpawnSpec(
                spawnCode = 9,
                kind = BallKind.ICE,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 1f
            )
        }
    }

    private fun updateFallingBalls() {
        val config = uiState.levelConfig
        val slowFactor = currentSlowMotionFactor()

        val updatedBalls = mutableListOf<BallItem>()
        val spawnedFromSplit = mutableListOf<BallItem>()

        uiState.balls.forEach { ball ->
            if (ball.isDragging) {
                updatedBalls += ball
                return@forEach
            }

            val nextVelocityX = if (ball.rimRejected) {
                ball.velocityX * 0.985f
            } else {
                ball.velocityX * 0.90f
            }

            val nextVelocityY = if (ball.rimRejected) {
                ball.velocityY * 0.96f
            } else {
                ball.velocityY * 0.90f
            }

            val movementOffsetX = if (ball.lockedToPattern || ball.rimRejected) {
                0f
            } else {
                when (ball.movementType) {
                    BallMovementType.STRAIGHT -> 0f
                    BallMovementType.DRIFT_LEFT,
                    BallMovementType.DRIFT_RIGHT,
                    BallMovementType.DIAGONAL_LEFT,
                    BallMovementType.DIAGONAL_RIGHT -> ball.driftX * currentDriftMultiplier()

                    BallMovementType.ZIGZAG_SOFT -> {
                        sin(ball.y * config.zigzagFrequency + ball.movementPhase) *
                                config.zigzagAmplitude * 0.35f
                    }

                    BallMovementType.ZIGZAG_HARD -> {
                        sin(ball.y * (config.zigzagFrequency * 1.2f) + ball.movementPhase) *
                                config.zigzagAmplitude * 0.55f
                    }
                }
            }

            val rejectedExtraDrop = if (ball.rimRejected) 1f else 0f

            val rawNextX = ball.x + movementOffsetX + nextVelocityX
            val horizontalBoundsMin = ball.radius
            val horizontalBoundsMax = uiState.canvasWidth - ball.radius
            val hitLeftWall = rawNextX <= horizontalBoundsMin
            val hitRightWall = rawNextX >= horizontalBoundsMax
            val wallBounced = hitLeftWall || hitRightWall

            val nextX = rawNextX.coerceIn(horizontalBoundsMin, horizontalBoundsMax)

            val nextY = ball.y +
                    ((ball.speed * currentFallMultiplier()) * slowFactor) +
                    (nextVelocityY * slowFactor) +
                    rejectedExtraDrop

            val bouncedVelocityX = if (wallBounced) {
                -nextVelocityX.coerceIn(-18f, 18f)
            } else {
                nextVelocityX
            }

            val bouncedDriftX = if (wallBounced && !ball.lockedToPattern) {
                -ball.driftX
            } else {
                ball.driftX
            }

            val bouncedMovementType = if (wallBounced) {
                when (ball.movementType) {
                    BallMovementType.DRIFT_LEFT -> BallMovementType.DRIFT_RIGHT
                    BallMovementType.DRIFT_RIGHT -> BallMovementType.DRIFT_LEFT
                    BallMovementType.DIAGONAL_LEFT -> BallMovementType.DIAGONAL_RIGHT
                    BallMovementType.DIAGONAL_RIGHT -> BallMovementType.DIAGONAL_LEFT
                    else -> ball.movementType
                }
            } else {
                ball.movementType
            }

            val bouncedMovementPhase = if (wallBounced && !ball.lockedToPattern) {
                when (ball.movementType) {
                    BallMovementType.ZIGZAG_SOFT,
                    BallMovementType.ZIGZAG_HARD -> -ball.movementPhase
                    else -> ball.movementPhase
                }
            } else {
                ball.movementPhase
            }

            val nextScale = when {
                ball.justCaptured -> (ball.scale - 0.08f).coerceAtLeast(0.76f)
                ball.scale < 1f -> (ball.scale + 0.05f).coerceAtMost(1f)
                ball.scale > 1f -> (ball.scale - 0.04f).coerceAtLeast(1f)
                else -> 1f
            }

            var movedBall = ball.copy(
                x = nextX,
                y = nextY,
                driftX = bouncedDriftX,
                velocityX = bouncedVelocityX,
                velocityY = nextVelocityY,
                rotationDeg = ball.rotationDeg + ball.rotationSpeedDeg,
                scale = nextScale,
                movementType = bouncedMovementType,
                movementPhase = bouncedMovementPhase,
                justCaptured = false,
                rimBounceCooldownFrames = (ball.rimBounceCooldownFrames - 1).coerceAtLeast(0)
            )

            movedBall = updateColorShiftBall(movedBall)

            if (
                movedBall.kind == BallKind.SPLIT &&
                !movedBall.splitTriggered &&
                movedBall.y >= uiState.canvasHeight * 0.55f
            ) {
                spawnedFromSplit += createSplitChildren(movedBall)
                return@forEach
            }

            val finalBall = if (movedBall.rimRejected) {
                movedBall
            } else {
                applyBasketPointPhysics(movedBall)
            }

            updatedBalls += finalBall
        }

        uiState = uiState.copy(
            balls = updatedBalls + spawnedFromSplit
        )
    }

    private fun updateColorShiftBall(ball: BallItem): BallItem {
        if (ball.kind != BallKind.COLOR_SHIFT) return ball

        val basketsTop = uiState.baskets.minOfOrNull { basket -> basket.top }
            ?: (uiState.canvasHeight * 0.82f)

        val distanceToBasketLine = basketsTop - ball.y

        val revealStartDistance = (uiState.canvasHeight * 0.42f).coerceAtLeast(260f)
        val revealLockDistance = (uiState.canvasHeight * 0.20f).coerceAtLeast(120f)

        return when {
            distanceToBasketLine <= revealLockDistance -> {
                ball.copy(
                    color = ball.targetColor,
                    colorRevealLocked = true
                )
            }

            distanceToBasketLine <= revealStartDistance -> {
                val progress = (
                        (revealStartDistance - distanceToBasketLine) /
                                (revealStartDistance - revealLockDistance)
                        ).coerceIn(0f, 1f)

                val shouldRevealNow = progress >= 0.72f

                if (shouldRevealNow) {
                    ball.copy(
                        color = ball.targetColor,
                        colorRevealLocked = true
                    )
                } else {
                    val activeColors = activePlayableColors()
                    val rotatingColors = activeColors.filter { color ->
                        color != ball.targetColor
                    }

                    val displayColor = if (rotatingColors.isEmpty()) {
                        ball.targetColor
                    } else {
                        val rawIndex = ((ball.y / 18f) + (ball.id * 0.7f)).toInt()
                        val safeIndex = ((rawIndex % rotatingColors.size) + rotatingColors.size) % rotatingColors.size
                        rotatingColors[safeIndex]
                    }

                    ball.copy(
                        color = displayColor,
                        colorRevealLocked = false
                    )
                }
            }

            else -> {
                val activeColors = activePlayableColors()
                val rotatingColors = activeColors.filter { color ->
                    color != ball.targetColor
                }

                val displayColor = if (rotatingColors.isEmpty()) {
                    ball.targetColor
                } else {
                    val rawIndex = ((ball.y / 14f) + (ball.id * 1.1f)).toInt()
                    val safeIndex = ((rawIndex % rotatingColors.size) + rotatingColors.size) % rotatingColors.size
                    rotatingColors[safeIndex]
                }

                ball.copy(
                    color = displayColor,
                    colorRevealLocked = false
                )
            }
        }
    }

    private fun activePlayableColors(): List<BallColor> {
        return when (uiState.colorCountForActivePart) {
            3 -> listOf(BallColor.BLUE, BallColor.RED, BallColor.GREEN)
            else -> listOf(BallColor.BLUE, BallColor.RED)
        }
    }

    private fun createSplitChildren(parent: BallItem): List<BallItem> {
        val childRadius = (parent.radius * 0.92f).coerceAtLeast(22f)
        val spreadX = childRadius * 1.2f

        return listOf(
            parent.copy(
                id = nextBallId++,
                kind = BallKind.NORMAL,
                spawnCode = 1,
                color = parent.targetColor,
                targetColor = parent.targetColor,
                x = (parent.x - spreadX).coerceIn(childRadius, uiState.canvasWidth - childRadius),
                y = parent.y,
                radius = childRadius,
                velocityX = -3.5f,
                velocityY = -1.5f,
                splitTriggered = true,
                colorRevealLocked = true
            ),
            parent.copy(
                id = nextBallId++,
                kind = BallKind.NORMAL,
                spawnCode = 1,
                color = parent.targetColor,
                targetColor = parent.targetColor,
                x = (parent.x + spreadX).coerceIn(childRadius, uiState.canvasWidth - childRadius),
                y = parent.y,
                radius = childRadius,
                velocityX = 3.5f,
                velocityY = -1.5f,
                splitTriggered = true,
                colorRevealLocked = true
            )
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

        var shapeRemaining = uiState.shapeRemainingCount
        var shapeIds = uiState.activeShapeBallIds

        uiState.balls.forEach { ball ->
            if (ball.isDragging) return@forEach

            val hitBasket = uiState.baskets.firstOrNull { basket ->
                isBallInsideBasket(ball, basket)
            } ?: return@forEach

            caughtBallIds += ball.id
            hitBasketColors += hitBasket.color

            if (ball.id in shapeIds) {
                shapeRemaining -= 1
                shapeIds = shapeIds - ball.id
            }

            when (ball.kind) {
                BallKind.BONUS -> {
                    val addCount = 5
                    correctDelta += addCount
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

                    newPopups += createPopup(
                        text = "+5",
                        x = ball.x,
                        y = ball.y - (ball.radius * 1.35f),
                        isPositive = true,
                        isCombo = true
                    )

                    newFeedbackEvents += createFeedbackEvent(GameFeedbackType.CORRECT)
                    triggerImpactOverlay(GameFeedbackType.CORRECT)
                }

                BallKind.BOMB -> {
                    wrongDelta += 2
                    comboCount = 0
                    lastCorrectCatchMillis = 0L

                    newPopups += createPopup(
                        text = "BOMBA -2 CAN",
                        x = ball.x,
                        y = ball.y - (ball.radius * 1.35f),
                        isPositive = false,
                        isCombo = true
                    )

                    newFeedbackEvents += createFeedbackEvent(GameFeedbackType.WRONG)
                    triggerImpactOverlay(GameFeedbackType.WRONG)
                }

                BallKind.ICE -> {
                    correctDelta += 1
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
                    slowMotionUntilMillis = now + 3000L

                    newPopups += createPopup(
                        text = "BUZ",
                        x = ball.x,
                        y = ball.y - (ball.radius * 1.35f),
                        isPositive = true,
                        isCombo = true
                    )

                    newFeedbackEvents += createFeedbackEvent(GameFeedbackType.CORRECT)
                    triggerImpactOverlay(GameFeedbackType.CORRECT)
                }

                BallKind.NORMAL,
                BallKind.COLOR_SHIFT,
                BallKind.SPLIT -> {
                    if (ball.targetColor == hitBasket.color) {
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

                        val totalPoints = uiState.levelConfig.correctBaseScore + (comboBonusSteps * 5)

                        scoreDelta += totalPoints
                        correctDelta += 1

                        newPopups += createPopup(
                            text = "+$totalPoints",
                            x = ball.x,
                            y = ball.y - (ball.radius * 1.35f),
                            isPositive = true,
                            isCombo = false
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
                        triggerImpactOverlay(GameFeedbackType.CORRECT)
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
                        triggerImpactOverlay(GameFeedbackType.WRONG)
                    }
                }
            }
        }

        if (caughtBallIds.isEmpty()) return

        val shapeCompleted = uiState.isShapeActive && shapeRemaining <= 0
        if (shapeCompleted) {
            scoreDelta += 50

            newPopups += createPopup(
                text = "SHAPE BONUS +50",
                x = uiState.canvasWidth / 2f,
                y = uiState.canvasHeight * 0.3f,
                isPositive = true,
                isCombo = true
            )

            newFeedbackEvents += createFeedbackEvent(GameFeedbackType.CORRECT)
            triggerImpactOverlay(GameFeedbackType.CORRECT)

            shapeRemaining = 0
            shapeIds = emptySet()
        }

        val target = uiState.targetCorrectCountForActivePart
        val newCorrectCount = (uiState.correctPlacedCount + correctDelta).coerceAtMost(target)
        val partCompleted = newCorrectCount >= target

        uiState = uiState.copy(
            balls = uiState.balls.filterNot { it.id in caughtBallIds },
            score = (uiState.score + scoreDelta).coerceAtLeast(0),
            correctPlacedCount = newCorrectCount,
            totalCorrectCount = uiState.totalCorrectCount + correctDelta,
            wrongCount = uiState.wrongCount + wrongDelta,
            comboCount = comboCount,
            bestCombo = bestCombo,
            activeShapeBallIds = shapeIds,
            shapeRemainingCount = shapeRemaining.coerceAtLeast(0),
            isShapeActive = shapeIds.isNotEmpty(),
            feedbackPopups = uiState.feedbackPopups + newPopups,
            feedbackEvents = uiState.feedbackEvents + newFeedbackEvents,
            baskets = uiState.baskets.map { basket ->
                if (basket.color in hitBasketColors) {
                    basket.copy(glowAlpha = 1f)
                } else {
                    basket
                }
            }
        )

        if (partCompleted) {
            handlePartCompleted()
        }
    }

    private fun handlePartCompleted() {
        val totalPartCount = uiState.levelConfig.parts.size.coerceAtLeast(1)
        val isLastPart = uiState.currentPart >= totalPartCount

        if (isLastPart) {
            uiState = uiState.copy(
                status = GameStatus.LEVEL_COMPLETED,
                feedbackEvents = uiState.feedbackEvents + createFeedbackEvent(GameFeedbackType.LEVEL_COMPLETE)
            )
            triggerImpactOverlay(GameFeedbackType.LEVEL_COMPLETE)
            return
        }

        val nextPart = uiState.currentPart + 1
        val nextPartConfig = uiState.levelConfig.partOrFirst(nextPart)

        draggingBallId = null
        lastCorrectCatchMillis = 0L
        lastSpawnTimeMillis = System.currentTimeMillis()
        lastPointerX = 0f
        lastPointerY = 0f
        lastDragDeltaX = 0f
        lastDragDeltaY = 0f
        dragTouchOffsetX = 0f
        dragTouchOffsetY = 0f
        slowMotionUntilMillis = 0L
        flowEngine = GameFlowEngine(nextPartConfig)
        resetBasketMotionState()

        val rebuiltBaskets = if (uiState.canvasWidth > 0f && uiState.canvasHeight > 0f) {
            createBaskets(
                width = uiState.canvasWidth,
                height = uiState.canvasHeight,
                colorCount = nextPartConfig.colorCount
            )
        } else {
            emptyList()
        }

        basketLeftLane = rebuiltBaskets.getOrNull(0)
        basketRightLane = rebuiltBaskets.getOrNull(1)

        uiState = uiState.copy(
            currentPart = nextPart,
            correctPlacedCount = 0,
            missedCount = 0,
            wrongCount = 0,
            comboCount = 0,
            maxMistakesAllowed = nextPartConfig.maxMistakesAllowed,
            balls = emptyList(),
            activeShapeBallIds = emptySet(),
            shapeRemainingCount = 0,
            isShapeActive = false,
            feedbackPopups = emptyList(),
            baskets = snapBasketsToMotionStart(
                rebuiltBaskets.map { it.copy(glowAlpha = 0f) }
            ),
            status = GameStatus.PLAYING
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

        var shapeRemaining = uiState.shapeRemainingCount
        var shapeIds = uiState.activeShapeBallIds

        missedBalls.forEach { ball ->
            if (ball.id in shapeIds) {
                shapeRemaining -= 1
                shapeIds = shapeIds - ball.id
            }
        }

        val lifePenaltyMissed = missedBalls.filter {
            it.kind == BallKind.NORMAL ||
                    it.kind == BallKind.BONUS ||
                    it.kind == BallKind.COLOR_SHIFT ||
                    it.kind == BallKind.SPLIT
        }

        val neutralMissed = missedBalls.filter {
            it.kind == BallKind.BOMB || it.kind == BallKind.ICE
        }

        val missPopups = mutableListOf<GameFeedbackPopup>()

        lifePenaltyMissed.forEach { ball ->
            missPopups += createPopup(
                text = "-${uiState.levelConfig.missPenalty}",
                x = ball.x,
                y = (uiState.canvasHeight - 36f).coerceAtLeast(24f),
                isPositive = false,
                isCombo = false
            )
        }

        neutralMissed.forEach { ball ->
            missPopups += createPopup(
                text = when (ball.kind) {
                    BallKind.BOMB -> "BOMBA KAÇTI"
                    BallKind.ICE -> "BUZ KAÇTI"
                    else -> ""
                },
                x = ball.x,
                y = (uiState.canvasHeight - 36f).coerceAtLeast(24f),
                isPositive = true,
                isCombo = false
            )
        }

        val normalMissCount = lifePenaltyMissed.size
        val scoreAfterPenalty = uiState.score - (normalMissCount * uiState.levelConfig.missPenalty)

        if (normalMissCount > 0) {
            lastCorrectCatchMillis = 0L
        }

        uiState = uiState.copy(
            balls = remainingBalls,
            missedCount = uiState.missedCount + normalMissCount,
            score = scoreAfterPenalty.coerceAtLeast(0),
            comboCount = if (normalMissCount > 0) 0 else uiState.comboCount,
            activeShapeBallIds = shapeIds,
            shapeRemainingCount = shapeRemaining.coerceAtLeast(0),
            isShapeActive = shapeIds.isNotEmpty(),
            feedbackPopups = uiState.feedbackPopups + missPopups,
            feedbackEvents = uiState.feedbackEvents +
                    if (normalMissCount > 0) {
                        listOf(createFeedbackEvent(GameFeedbackType.MISS))
                    } else {
                        emptyList()
                    }
        )

        if (normalMissCount > 0) {
            triggerImpactOverlay(GameFeedbackType.MISS)
        }
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

    private fun triggerImpactOverlay(type: GameFeedbackType) {
        val startAlpha = when (type) {
            GameFeedbackType.CORRECT -> 0.16f
            GameFeedbackType.WRONG -> 0.22f
            GameFeedbackType.MISS -> 0.18f
            GameFeedbackType.LEVEL_COMPLETE -> 0.24f
            GameFeedbackType.GAME_OVER -> 0.30f
        }

        uiState = uiState.copy(
            impactOverlay = GameImpactOverlay(
                type = type,
                alpha = startAlpha
            )
        )
    }

    private fun updateImpactOverlay() {
        val overlay = uiState.impactOverlay ?: return

        val nextAlpha = when (overlay.type) {
            GameFeedbackType.CORRECT -> overlay.alpha - 0.018f
            GameFeedbackType.WRONG -> overlay.alpha - 0.024f
            GameFeedbackType.MISS -> overlay.alpha - 0.022f
            GameFeedbackType.LEVEL_COMPLETE -> overlay.alpha - 0.016f
            GameFeedbackType.GAME_OVER -> overlay.alpha - 0.014f
        }

        uiState = uiState.copy(
            impactOverlay = if (nextAlpha <= 0.01f) null else overlay.copy(alpha = nextAlpha)
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
            slowMotionUntilMillis = 0L

            uiState = uiState.copy(
                balls = emptyList(),
                comboCount = 0,
                activeShapeBallIds = emptySet(),
                shapeRemainingCount = 0,
                isShapeActive = false,
                status = GameStatus.GAME_OVER,
                sessionSummary = GameSessionSummary(
                    score = uiState.score,
                    reachedLevel = uiState.currentLevel,
                    totalCorrect = uiState.totalCorrectCount,
                    totalMistakes = uiState.totalMistakes,
                    wrongBasketCount = uiState.wrongCount,
                    missedBallCount = uiState.missedCount,
                    bestCombo = uiState.bestCombo,
                    accuracyPercent = uiState.accuracyPercent,
                    durationSeconds = durationSeconds
                ),
                feedbackEvents = uiState.feedbackEvents + createFeedbackEvent(GameFeedbackType.GAME_OVER)
            )
            triggerImpactOverlay(GameFeedbackType.GAME_OVER)
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

    private fun createFeedbackEvent(type: GameFeedbackType): GameFeedbackEvent {
        return GameFeedbackEvent(
            id = nextFeedbackEventId++,
            type = type
        )
    }

    private fun applyBasketPointPhysics(ball: BallItem): BallItem {
        if (uiState.baskets.isEmpty()) return ball
        if (ball.rimBounceCooldownFrames > 0) return ball
        if (ball.rimRejected) return ball

        var result = ball

        uiState.baskets.forEach { basket ->
            val basketWidth = basket.right - basket.left
            val basketHeight = basket.bottom - basket.top

            val leftPointX = basket.left + (basketWidth * 0.12f)
            val rightPointX = basket.right - (basketWidth * 0.12f)
            val pointY = basket.top + (basketHeight * 0.04f)

            val pointRadius = basketWidth * 0.022f

            val scoringLeft = leftPointX + pointRadius * 1.6f
            val scoringRight = rightPointX - pointRadius * 1.6f

            val ballBottom = result.y + result.radius

            val alreadyInsideScoringCorridor =
                result.x in scoringLeft..scoringRight &&
                        ballBottom > basket.top + (basketHeight * 0.08f)

            if (alreadyInsideScoringCorridor) return@forEach

            val incomingVX = result.velocityX
            val incomingVY =
                (result.speed * currentFallMultiplier() + result.velocityY).coerceAtLeast(2.5f)

            val incomingSpeed = hypot(incomingVX, incomingVY).coerceAtLeast(4f)

            if (incomingVY <= 0.5f) return@forEach

            fun resolvePointHit(
                pointCenterX: Float,
                isLeftPoint: Boolean
            ): BallItem? {
                val dx = result.x - pointCenterX
                val dy = result.y - pointY
                val distance = hypot(dx, dy)

                val touching = distance <= (result.radius + pointRadius)
                if (!touching) return null

                val rawNormalX = if (distance > 0.001f) dx / distance else if (isLeftPoint) -1f else 1f
                val rawNormalY = if (distance > 0.001f) dy / distance else -0.15f

                val normalLength = hypot(rawNormalX, rawNormalY).coerceAtLeast(0.0001f)
                val nx = rawNormalX / normalLength
                val ny = rawNormalY / normalLength

                val dot = incomingVX * nx + incomingVY * ny
                val reflectedX = incomingVX - 2f * dot * nx
                val reflectedY = incomingVY - 2f * dot * ny

                val goesTowardCenter = if (isLeftPoint) {
                    reflectedX > 0.20f
                } else {
                    reflectedX < -0.20f
                }

                return if (goesTowardCenter) {
                    val inwardX = maxOf(abs(reflectedX), incomingSpeed * 0.8f)
                    val inwardHopY = maxOf(incomingSpeed * 5.50f, 18f)

                    result.copy(
                        x = if (isLeftPoint) {
                            scoringLeft + result.radius * 0.95f
                        } else {
                            scoringRight - result.radius * 0.95f
                        },
                        y = basket.top + (basketHeight * 0.10f),
                        velocityX = if (isLeftPoint) inwardX else -inwardX,
                        velocityY = -inwardHopY,
                        rimBounceCooldownFrames = 7
                    )
                } else {
                    val outwardX = maxOf(abs(reflectedX), incomingSpeed * 0.2f)
                    val outwardHopY = maxOf(incomingSpeed * 2f, 12f)

                    result.copy(
                        x = if (isLeftPoint) {
                            basket.left - result.radius * 0.55f
                        } else {
                            basket.right + result.radius * 0.55f
                        },
                        y = basket.top - result.radius * 0.10f,
                        velocityX = if (isLeftPoint) -outwardX else outwardX,
                        velocityY = -outwardHopY,
                        rimBounceCooldownFrames = 10,
                        rimRejected = true
                    )
                }
            }

            val resolvedLeft = resolvePointHit(
                pointCenterX = leftPointX,
                isLeftPoint = true
            )

            if (resolvedLeft != null) {
                result = resolvedLeft
                return@forEach
            }

            val resolvedRight = resolvePointHit(
                pointCenterX = rightPointX,
                isLeftPoint = false
            )

            if (resolvedRight != null) {
                result = resolvedRight
                return@forEach
            }
        }

        return result
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
        if (ball.rimRejected) return false

        val basketWidth = basket.right - basket.left
        val basketHeight = basket.bottom - basket.top

        val leftPointX = basket.left + (basketWidth * 0.15f)
        val rightPointX = basket.right - (basketWidth * 0.15f)
        val pointRadius = basketWidth * 0.022f

        val scoringLeft = leftPointX + pointRadius * 1.4f
        val scoringRight = rightPointX - pointRadius * 1.4f

        val captureTop = basket.top + (basketHeight * 0.10f)
        val captureBottom = basket.top + (basketHeight * 0.52f)

        val centerInsideOpening = ball.x in scoringLeft..scoringRight
        val ballBottom = ball.y + ball.radius
        val insideCaptureBand = ballBottom in captureTop..captureBottom

        val descending = (ball.speed + ball.velocityY) > 0.1f

        return centerInsideOpening && insideCaptureBand && descending
    }

    private fun currentSpawnIntervalMillis(): Long {
        return activePartConfig().spawnIntervalMillis.coerceAtLeast(260L)
    }

    private fun currentFallMultiplier(): Float {
        return 1f
    }

    private fun currentSlowMotionFactor(): Float {
        return if (System.currentTimeMillis() < slowMotionUntilMillis) 0.35f else 1f
    }

    private fun currentDriftMultiplier(): Float {
        return 1f
    }

    private fun chooseSpawnPatternType(): SpawnPatternType {
        val config = uiState.levelConfig
        val patterns = config.allowedPatternTypes

        if (patterns.isEmpty()) return SpawnPatternType.SINGLE

        val featuredPattern = config.featuredPatternType
        val featuredChance = config.featuredPatternChance

        if (
            featuredPattern != null &&
            featuredPattern in patterns &&
            Random.nextFloat() < featuredChance
        ) {
            return featuredPattern
        }

        val shouldUseMultiBallWave =
            patterns.any { it != SpawnPatternType.SINGLE } &&
                    Random.nextFloat() < config.multiBallWaveChance

        if (!shouldUseMultiBallWave) {
            return SpawnPatternType.SINGLE
        }

        val multiPatterns = patterns.filter { it != SpawnPatternType.SINGLE }
        if (multiPatterns.isEmpty()) return SpawnPatternType.SINGLE

        return multiPatterns.random()
    }

    private fun buildSpawnPositions(
        patternType: SpawnPatternType,
        radius: Float,
        minX: Float,
        maxX: Float
    ): List<SpawnPoint> {
        val safeWidth = maxX - minX
        if (safeWidth <= 0f) return emptyList()

        val horizontalGap = radius * 2.6f
        val verticalGap = radius * 2.4f

        return when (patternType) {
            SpawnPatternType.SINGLE -> {
                val centerX = Random.nextFloat() * safeWidth + minX
                listOf(SpawnPoint(centerX, radius + 8f))
            }

            SpawnPatternType.COLUMN -> {
                val centerX = Random.nextFloat() * safeWidth + minX
                listOf(
                    SpawnPoint(centerX, radius + 8f),
                    SpawnPoint(centerX, radius + 8f + verticalGap),
                    SpawnPoint(centerX, radius + 8f + verticalGap * 2f)
                )
            }

            SpawnPatternType.V_SHAPE -> {
                val centerX = (Random.nextFloat() * (safeWidth - horizontalGap * 2f) + minX + horizontalGap)
                    .coerceIn(minX + horizontalGap, maxX - horizontalGap)

                listOf(
                    SpawnPoint(centerX, radius + 8f),
                    SpawnPoint(centerX - horizontalGap, radius + 8f + verticalGap),
                    SpawnPoint(centerX + horizontalGap, radius + 8f + verticalGap),
                    SpawnPoint(centerX - horizontalGap * 2f, radius + 8f + verticalGap * 2f),
                    SpawnPoint(centerX + horizontalGap * 2f, radius + 8f + verticalGap * 2f)
                ).filter { it.x in minX..maxX }
            }

            SpawnPatternType.SQUARE -> {
                val leftX = (Random.nextFloat() * (safeWidth - horizontalGap) + minX)
                    .coerceIn(minX, maxX - horizontalGap)

                val rightX = (leftX + horizontalGap).coerceAtMost(maxX)

                listOf(
                    SpawnPoint(leftX, radius + 8f),
                    SpawnPoint(rightX, radius + 8f),
                    SpawnPoint(leftX, radius + 8f + verticalGap),
                    SpawnPoint(rightX, radius + 8f + verticalGap)
                )
            }

            SpawnPatternType.ARC -> {
                val centerX = (Random.nextFloat() * (safeWidth - horizontalGap * 4f) + minX + horizontalGap * 2f)
                    .coerceIn(minX + horizontalGap * 2f, maxX - horizontalGap * 2f)

                listOf(
                    SpawnPoint(centerX - horizontalGap * 2f, radius + 8f + verticalGap * 2f),
                    SpawnPoint(centerX - horizontalGap, radius + 8f + verticalGap),
                    SpawnPoint(centerX, radius + 8f),
                    SpawnPoint(centerX + horizontalGap, radius + 8f + verticalGap),
                    SpawnPoint(centerX + horizontalGap * 2f, radius + 8f + verticalGap * 2f)
                ).filter { it.x in minX..maxX }
            }
        }
    }

    private fun chooseSpawnSpec(entries: List<SpawnMixEntry>): SpawnSpec {
        val selectedCode = weightedSpawnCode(entries)

        return when (selectedCode) {
            2 -> SpawnSpec(
                spawnCode = 2,
                kind = BallKind.BONUS,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 1f
            )

            3 -> SpawnSpec(
                spawnCode = 3,
                kind = BallKind.BOMB,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 1f
            )

            4 -> SpawnSpec(
                spawnCode = 4,
                kind = BallKind.NORMAL,
                movementType = if (Random.nextBoolean()) {
                    BallMovementType.DIAGONAL_LEFT
                } else {
                    BallMovementType.DIAGONAL_RIGHT
                },
                speedMultiplier = 1f
            )

            5 -> SpawnSpec(
                spawnCode = 5,
                kind = BallKind.NORMAL,
                movementType = BallMovementType.ZIGZAG_SOFT,
                speedMultiplier = 1f
            )

            6 -> SpawnSpec(
                spawnCode = 6,
                kind = BallKind.NORMAL,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 1.35f
            )

            7 -> SpawnSpec(
                spawnCode = 7,
                kind = BallKind.NORMAL,
                movementType = BallMovementType.ZIGZAG_HARD,
                speedMultiplier = 1f
            )

            8 -> SpawnSpec(
                spawnCode = 8,
                kind = BallKind.NORMAL,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 0.72f
            )

            9 -> SpawnSpec(
                spawnCode = 9,
                kind = BallKind.ICE,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 1f
            )

            11 -> SpawnSpec(
                spawnCode = 11,
                kind = BallKind.COLOR_SHIFT,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 1f
            )

            13 -> SpawnSpec(
                spawnCode = 13,
                kind = BallKind.SPLIT,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 1f
            )

            else -> SpawnSpec(
                spawnCode = 1,
                kind = BallKind.NORMAL,
                movementType = BallMovementType.STRAIGHT,
                speedMultiplier = 1f
            )
        }
    }

    private fun weightedSpawnCode(entries: List<SpawnMixEntry>): Int {
        val total = entries.sumOf { it.weightPercent.coerceAtLeast(0) }
        if (total <= 0) return 1

        val roll = Random.nextInt(total)
        var cursor = 0

        entries.forEach { entry ->
            cursor += entry.weightPercent.coerceAtLeast(0)
            if (roll < cursor) {
                return entry.spawnCode
            }
        }

        return entries.lastOrNull()?.spawnCode ?: 1
    }

    private fun randomBallColor(): BallColor {
        return activePlayableColors().random()
    }

    private fun randomDifferentColor(from: BallColor): BallColor {
        return activePlayableColors()
            .filter { it != from }
            .random()
    }

    private fun updateBasketMotion() {
        if (uiState.baskets.size < 2) return

        when (activePartConfig().basketMotionMode) {
            BasketMotionMode.NONE -> {
                val snapped = snapBasketsToMotionStart(uiState.baskets)
                if (snapped != uiState.baskets) {
                    uiState = uiState.copy(baskets = snapped)
                }
            }

            BasketMotionMode.HORIZONTAL_SWAY -> {
                updateHorizontalSwayMotion()
            }

            BasketMotionMode.SWAP_EVERY_10_CORRECT -> {
                updateSwapMotion()
            }
        }
    }

    private fun basketRightLaneForThreeColors(): Basket {
        val baskets = createBaskets(
            width = uiState.canvasWidth,
            height = uiState.canvasHeight,
            colorCount = activePartConfig().colorCount
        )
        return baskets.last()
    }

    private fun updateHorizontalSwayMotion() {
        val leftLane = basketLeftLane ?: return
        val rightLane = basketRightLane ?: return
        if (uiState.baskets.size < 2) return

        val amplitude = (uiState.canvasWidth * 0.025f).coerceIn(8f, 18f)
        val step = (uiState.canvasWidth * 0.0012f).coerceIn(0.6f, 1.4f)

        basketSwayOffset += step * basketSwayDirection
        if (basketSwayOffset >= amplitude) {
            basketSwayOffset = amplitude
            basketSwayDirection = -1f
        } else if (basketSwayOffset <= -amplitude) {
            basketSwayOffset = -amplitude
            basketSwayDirection = 1f
        }

        val updated = uiState.baskets.map { basket ->
            when (basket.color) {
                BallColor.BLUE -> moveBasketByOffset(leftLane, basketSwayOffset, basket.glowAlpha)
                BallColor.RED -> moveBasketByOffset(rightLane, basketSwayOffset, basket.glowAlpha)
                BallColor.GREEN -> moveBasketByOffset(
                    lane = basketRightLaneForThreeColors(),
                    offsetX = basketSwayOffset,
                    glowAlpha = basket.glowAlpha
                )
            }
        }

        uiState = uiState.copy(baskets = updated)
    }

    private fun updateSwapMotion() {
        val leftLane = basketLeftLane ?: return
        val rightLane = basketRightLane ?: return
        if (uiState.baskets.size < 2) return

        val threshold = uiState.correctPlacedCount / 10
        if (threshold > 0 && threshold != lastSwapThreshold) {
            basketSwapState = !basketSwapState
            lastSwapThreshold = threshold
        }

        val updated = uiState.baskets.map { basket ->
            val targetLane = when (basket.color) {
                BallColor.BLUE -> if (basketSwapState) rightLane else leftLane
                BallColor.RED -> if (basketSwapState) leftLane else rightLane
                BallColor.GREEN -> basketRightLaneForThreeColors()
            }

            moveBasketTowardLane(
                current = basket,
                target = targetLane
            )
        }

        uiState = uiState.copy(baskets = updated)
    }

    private fun moveBasketByOffset(
        lane: Basket,
        offsetX: Float,
        glowAlpha: Float
    ): Basket {
        val width = lane.right - lane.left
        return lane.copy(
            left = lane.left + offsetX,
            right = lane.left + offsetX + width,
            glowAlpha = glowAlpha
        )
    }

    private fun moveBasketTowardLane(
        current: Basket,
        target: Basket
    ): Basket {
        val width = current.right - current.left
        val lerpFactor = 0.10f

        val nextLeft = current.left + ((target.left - current.left) * lerpFactor)
        return current.copy(
            left = nextLeft,
            right = nextLeft + width
        )
    }

    private fun snapBasketsToMotionStart(
        baskets: List<Basket>
    ): List<Basket> {
        val leftLane = basketLeftLane
        val rightLane = basketRightLane

        if (leftLane == null || rightLane == null) return baskets
        if (baskets.size < 2) return baskets

        return baskets.map { basket ->
            val targetLane = when (activePartConfig().basketMotionMode) {
                BasketMotionMode.NONE,
                BasketMotionMode.HORIZONTAL_SWAY -> {
                    when (basket.color) {
                        BallColor.BLUE -> leftLane
                        BallColor.RED -> rightLane
                        BallColor.GREEN -> basketRightLaneForThreeColors()
                    }
                }

                BasketMotionMode.SWAP_EVERY_10_CORRECT -> {
                    when (basket.color) {
                        BallColor.BLUE -> if (basketSwapState) rightLane else leftLane
                        BallColor.RED -> if (basketSwapState) leftLane else rightLane
                        BallColor.GREEN -> basketRightLaneForThreeColors()
                    }
                }
            }

            targetLane.copy(glowAlpha = basket.glowAlpha)
        }
    }

    private fun resetBasketMotionState() {
        basketSwayOffset = 0f
        basketSwayDirection = 1f
        basketSwapState = false
        lastSwapThreshold = 0
    }

    private data class SpawnPoint(
        val x: Float,
        val y: Float
    )

    private data class SpawnSpec(
        val spawnCode: Int,
        val kind: BallKind,
        val movementType: BallMovementType,
        val speedMultiplier: Float
    )

    private fun currentLevelConfig(level: Int): LevelConfig {
        return DifficultyResolver.resolve(level)
    }

    private fun resetSessionForLevel(level: Int) {
        val preservedWidth = uiState.canvasWidth
        val preservedHeight = uiState.canvasHeight
        val safeLevel = level.coerceAtLeast(1)
        val newLevelConfig = currentLevelConfig(safeLevel)
        val firstPartConfig = newLevelConfig.partOrFirst(1)

        val preservedBaskets = if (preservedWidth > 0f && preservedHeight > 0f) {
            createBaskets(
                width = preservedWidth,
                height = preservedHeight,
                colorCount = firstPartConfig.colorCount
            )
        } else {
            emptyList()
        }

        basketLeftLane = preservedBaskets.getOrNull(0)
        basketRightLane = preservedBaskets.getOrNull(1)

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
        slowMotionUntilMillis = 0L
        flowEngine = null
        resetBasketMotionState()

        uiState = GameUiState(
            currentLevel = safeLevel,
            currentPart = 1,
            levelConfig = newLevelConfig,
            maxMistakesAllowed = firstPartConfig.maxMistakesAllowed,
            canvasWidth = preservedWidth,
            canvasHeight = preservedHeight,
            baskets = preservedBaskets
        )
    }

    private fun createBaskets(
        width: Float,
        height: Float,
        colorCount: Int
    ): List<Basket> {
        val safeColorCount = colorCount.coerceAtLeast(2)

        val basketWidth = width * 0.33f
        val basketHeight = height * 0.12f

        val spacing = if (safeColorCount == 3) {
            width * 0.03f
        } else {
            width * 0.06f
        }

        val totalWidth = (basketWidth * safeColorCount) + (spacing * (safeColorCount - 1))
        val startX = ((width - totalWidth) / 2f).coerceAtLeast(0f)
        val top = height - basketHeight - (height * 0.14f)

        val colors = when (safeColorCount) {
            3 -> listOf(BallColor.BLUE, BallColor.RED, BallColor.GREEN)
            else -> listOf(BallColor.BLUE, BallColor.RED)
        }

        return colors.mapIndexed { index, color ->
            val left = startX + index * (basketWidth + spacing)

            Basket(
                color = color,
                left = left,
                top = top,
                right = left + basketWidth,
                bottom = top + basketHeight
            )
        }
    }

    private fun activePartConfig(): LevelPartConfig {
        return uiState.levelConfig.partOrFirst(uiState.currentPart)
    }

    private fun activePartBaseFallSpeed(): Float {
        val percent = activePartConfig().baseFallSpeedPercent
        return 180f + (percent * 4.0f)
    }

    private fun activePartBaseDriftStrength(): Float {
        return 1.5f
    }

    private fun ensureFlowEngine(): GameFlowEngine {
        val existing = flowEngine
        if (existing != null) return existing

        val created = GameFlowEngine(activePartConfig())
        flowEngine = created
        return created
    }
}
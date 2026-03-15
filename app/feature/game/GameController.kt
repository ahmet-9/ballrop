package com.ballrop.app.feature.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ballrop.app.model.game.BallColor
import com.ballrop.app.model.game.BallItem
import com.ballrop.app.model.game.Basket
import com.ballrop.app.model.game.GameStatus
import com.ballrop.app.model.game.LevelConfig
import kotlin.random.Random

class GameController {

    var uiState by mutableStateOf(GameUiState())
        private set

    private var nextBallId = 0L
    private var lastSpawnTimeMillis = 0L

    fun onAction(action: GameAction) {
        when (action) {
            GameAction.StartGame -> {
                lastSpawnTimeMillis = System.currentTimeMillis()
                uiState = uiState.copy(
                    status = GameStatus.PLAYING
                )
            }

            GameAction.ResetGame -> {
                nextBallId = 0L
                lastSpawnTimeMillis = 0L
                uiState = GameUiState()
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

            GameAction.Tick -> {
                if (uiState.status != GameStatus.PLAYING) return
                if (uiState.canvasWidth <= 0f || uiState.canvasHeight <= 0f) return

                spawnBallIfNeeded()
                updateFallingBalls()
                resolveBasketHits()
                removeMissedBalls()
            }
        }
    }

    private fun spawnBallIfNeeded() {
        val now = System.currentTimeMillis()
        val interval = currentLevelConfig().spawnIntervalMillis

        if (now - lastSpawnTimeMillis < interval) return

        val radius = (uiState.canvasWidth * 0.045f).coerceIn(18f, 32f)
        val minX = radius
        val maxX = uiState.canvasWidth - radius

        if (maxX <= minX) return

        val randomX = Random.nextFloat() * (maxX - minX) + minX
        val color = if (Random.nextBoolean()) BallColor.BLUE else BallColor.RED

        val newBall = BallItem(
            id = nextBallId++,
            color = color,
            x = randomX,
            y = radius + 8f,
            radius = radius,
            speed = currentLevelConfig().baseFallSpeed / 60f
        )

        uiState = uiState.copy(
            balls = uiState.balls + newBall
        )

        lastSpawnTimeMillis = now
    }

    private fun updateFallingBalls() {
        uiState = uiState.copy(
            balls = uiState.balls.map { ball ->
                if (ball.isDragging) {
                    ball
                } else {
                    ball.copy(y = ball.y + ball.speed)
                }
            }
        )
    }

    private fun resolveBasketHits() {
        if (uiState.balls.isEmpty() || uiState.baskets.isEmpty()) return

        val caughtBallIds = mutableSetOf<Long>()
        var scoreDelta = 0
        var correctDelta = 0
        var wrongDelta = 0

        uiState.balls.forEach { ball ->
            val hitBasket = uiState.baskets.firstOrNull { basket ->
                isBallInsideBasket(ball, basket)
            }

            if (hitBasket != null) {
                caughtBallIds += ball.id

                if (ball.color == hitBasket.color) {
                    scoreDelta += 10
                    correctDelta += 1
                } else {
                    scoreDelta -= 5
                    wrongDelta += 1
                }
            }
        }

        if (caughtBallIds.isEmpty()) return

        val newCorrectCount = uiState.correctPlacedCount + correctDelta
        val target = currentLevelConfig().targetCorrectCount
        val levelCompleted = newCorrectCount >= target

        uiState = uiState.copy(
            balls = uiState.balls.filterNot { it.id in caughtBallIds },
            score = (uiState.score + scoreDelta).coerceAtLeast(0),
            correctPlacedCount = newCorrectCount,
            wrongCount = uiState.wrongCount + wrongDelta,
            status = if (levelCompleted) GameStatus.LEVEL_COMPLETED else uiState.status
        )
    }

    private fun removeMissedBalls() {
        if (uiState.balls.isEmpty()) return

        val remainingBalls = uiState.balls.filter { ball ->
            ball.y - ball.radius <= uiState.canvasHeight
        }

        val missedCountNow = uiState.balls.size - remainingBalls.size
        if (missedCountNow == 0) return

        uiState = uiState.copy(
            balls = remainingBalls,
            missedCount = uiState.missedCount + missedCountNow,
            score = (uiState.score - (missedCountNow * 5)).coerceAtLeast(0)
        )
    }

    private fun isBallInsideBasket(
        ball: BallItem,
        basket: Basket
    ): Boolean {
        val ballCenterX = ball.x
        val ballCenterY = ball.y

        return ballCenterX >= basket.left &&
                ballCenterX <= basket.right &&
                ballCenterY >= basket.top &&
                ballCenterY <= basket.bottom
    }

    private fun currentLevelConfig(): LevelConfig {
        val level = uiState.currentLevel

        return when {
            level <= 1 -> {
                LevelConfig(
                    targetCorrectCount = 20,
                    spawnIntervalMillis = 1200L,
                    baseFallSpeed = 240f
                )
            }

            level == 2 -> {
                LevelConfig(
                    targetCorrectCount = 20,
                    spawnIntervalMillis = 1000L,
                    baseFallSpeed = 280f
                )
            }

            else -> {
                LevelConfig(
                    targetCorrectCount = 20,
                    spawnIntervalMillis = 850L,
                    baseFallSpeed = 320f
                )
            }
        }
    }

    private fun createBaskets(width: Float, height: Float): List<Basket> {
        val horizontalPadding = width * 0.08f
        val basketSpacing = width * 0.06f
        val basketWidth = (width - (horizontalPadding * 2) - basketSpacing) / 2f
        val basketHeight = height * 0.12f
        val basketTop = height - basketHeight - (height * 0.05f)

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
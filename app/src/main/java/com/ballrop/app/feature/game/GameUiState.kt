package com.ballrop.app.feature.game

import com.ballrop.app.core.feedback.GameFeedbackEvent
import com.ballrop.app.model.game.BallItem
import com.ballrop.app.model.game.Basket
import com.ballrop.app.model.game.GameSessionSummary
import com.ballrop.app.model.game.GameStatus
import com.ballrop.app.model.game.LevelConfig
import com.ballrop.app.model.game.LevelPartConfig

data class GameUiState(
    val score: Int = 0,
    val correctPlacedCount: Int = 0,
    val totalCorrectCount: Int = 0,
    val missedCount: Int = 0,
    val wrongCount: Int = 0,
    val currentLevel: Int = 1,
    val currentPart: Int = 1,
    val status: GameStatus = GameStatus.READY,
    val levelConfig: LevelConfig = LevelConfig(),
    val maxMistakesAllowed: Int = 5,

    val comboCount: Int = 0,
    val bestCombo: Int = 0,

    val canvasWidth: Float = 0f,
    val canvasHeight: Float = 0f,

    val shapeTotalCount: Int = 0,

    val baskets: List<Basket> = emptyList(),
    val balls: List<BallItem> = emptyList(),

    val activeShapeBallIds: Set<Long> = emptySet(),
    val shapeRemainingCount: Int = 0,
    val isShapeActive: Boolean = false,

    val feedbackPopups: List<GameFeedbackPopup> = emptyList(),
    val feedbackEvents: List<GameFeedbackEvent> = emptyList(),
    val impactOverlay: GameImpactOverlay? = null,
    val sessionSummary: GameSessionSummary = GameSessionSummary()
) {
    val activePartConfig: LevelPartConfig
        get() = levelConfig.partOrFirst(currentPart)

    val targetCorrectCountForActivePart: Int
        get() = activePartConfig.targetCorrectCount

    val colorCountForActivePart: Int
        get() = activePartConfig.colorCount

    val totalPartCount: Int
        get() = levelConfig.parts.size.coerceAtLeast(1)

    val partDisplayText: String
        get() = "Part $currentPart/$totalPartCount"

    val totalMistakes: Int
        get() = wrongCount + missedCount

    val remainingLives: Int
        get() = (maxMistakesAllowed - totalMistakes).coerceAtLeast(0)

    val remainingTargetCount: Int
        get() = (targetCorrectCountForActivePart - correctPlacedCount).coerceAtLeast(0)

    val totalAttempts: Int
        get() = totalCorrectCount + totalMistakes

    val accuracyPercent: Int
        get() {
            if (totalAttempts <= 0) return 100
            return ((totalCorrectCount * 100f) / totalAttempts)
                .toInt()
                .coerceIn(0, 100)
        }

    val levelProgress: Float
        get() {
            if (targetCorrectCountForActivePart <= 0) return 0f
            return (correctPlacedCount.toFloat() / targetCorrectCountForActivePart.toFloat())
                .coerceIn(0f, 1f)
        }
}
package com.ballrop.app.feature.game

import com.ballrop.app.core.feedback.GameFeedbackEvent
import com.ballrop.app.model.game.BallItem
import com.ballrop.app.model.game.Basket
import com.ballrop.app.model.game.GameSessionSummary
import com.ballrop.app.model.game.GameStatus
import com.ballrop.app.model.game.LevelConfig

data class GameUiState(
    val score: Int = 0,
    val correctPlacedCount: Int = 0,
    val totalCorrectCount: Int = 0,
    val missedCount: Int = 0,
    val wrongCount: Int = 0,
    val currentLevel: Int = 1,
    val status: GameStatus = GameStatus.READY,
    val levelConfig: LevelConfig = LevelConfig(),
    val maxMistakesAllowed: Int = 8,

    val comboCount: Int = 0,
    val bestCombo: Int = 0,

    val canvasWidth: Float = 0f,
    val canvasHeight: Float = 0f,

    val baskets: List<Basket> = emptyList(),
    val balls: List<BallItem> = emptyList(),
    val feedbackPopups: List<GameFeedbackPopup> = emptyList(),
    val feedbackEvents: List<GameFeedbackEvent> = emptyList(),
    val impactOverlay: GameImpactOverlay? = null,
    val sessionSummary: GameSessionSummary = GameSessionSummary()
) {
    val totalMistakes: Int
        get() = wrongCount + missedCount

    val remainingLives: Int
        get() = (maxMistakesAllowed - totalMistakes).coerceAtLeast(0)

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
            if (levelConfig.targetCorrectCount <= 0) return 0f
            return (correctPlacedCount.toFloat() / levelConfig.targetCorrectCount.toFloat())
                .coerceIn(0f, 1f)
        }
}

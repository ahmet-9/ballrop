package com.ballrop.app.model.game

data class GameSceneSnapshot(
    val level: Int,
    val part: Int,

    val score: Int,
    val correctPlacedCount: Int,
    val totalCorrectCount: Int,
    val missedCount: Int,
    val wrongCount: Int,
    val comboCount: Int,
    val bestCombo: Int,

    val maxMistakesAllowed: Int,
    val targetCorrectCount: Int,

    val canvasWidth: Float,
    val canvasHeight: Float,

    val balls: List<BallItem>,
    val baskets: List<Basket>,

    val activeShapeBallIds: Set<Long>,
    val shapeRemainingCount: Int,
    val shapeTotalCount: Int,
    val isShapeActive: Boolean,

    val nextBallId: Long,
    val nextPopupId: Long,
    val nextFeedbackEventId: Long,

    val lastSpawnAgeMillis: Long,
    val lastCorrectCatchAgeMillis: Long,
    val slowMotionRemainingMillis: Long,
    val shapeCooldownRemainingMillis: Long,

    val flowBlockIndex: Int,
    val flowBlockSpawnCount: Int,
    val flowShapeConsumed: Boolean,
    val flowShapePreGapCompleted: Boolean,
    val flowShapePostGapCompleted: Boolean,

    val basketSwayOffset: Float,
    val basketSwayDirection: Float,
    val basketSwapState: Boolean,
    val lastSwapThreshold: Int
)
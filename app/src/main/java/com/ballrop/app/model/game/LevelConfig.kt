package com.ballrop.app.model.game

data class LevelConfig(
    val targetCorrectCount: Int = 20,
    val spawnIntervalMillis: Long = 1150L,
    val baseFallSpeed: Float = 240f,
    val comboResetWindowMillis: Long = 1800L,
    val maxComboBonusSteps: Int = 2,
    val horizontalDriftMultiplier: Float = 1f,
    val correctBaseScore: Int = 10,
    val wrongPenalty: Int = 5,
    val missPenalty: Int = 5,

    val introProgressThreshold: Float = 0.30f,
    val rushProgressThreshold: Float = 0.75f,

    val introSpawnMultiplier: Float = 1.18f,
    val introFallMultiplier: Float = 0.88f,

    val rushSpawnMultiplier: Float = 0.78f,
    val rushFallMultiplier: Float = 1.22f,
    val rushDriftMultiplier: Float = 1.18f,

    val bonusBallChance: Float = 0f,
    val dangerBallChance: Float = 0f,
    val bonusExtraScore: Int = 15,
    val dangerPenalty: Int = 12,
    val dangerEscapeScore: Int = 8,

    val allowedMovementTypes: List<BallMovementType> = listOf(BallMovementType.STRAIGHT),
    val zigzagAmplitude: Float = 14f,
    val zigzagFrequency: Float = 0.05f,
    val diagonalDriftStrength: Float = 1.2f,

    val allowedPatternTypes: List<SpawnPatternType> = listOf(SpawnPatternType.SINGLE),
    val multiBallWaveChance: Float = 0f,
    val featuredPatternType: SpawnPatternType? = null,
    val featuredPatternChance: Float = 0f
)
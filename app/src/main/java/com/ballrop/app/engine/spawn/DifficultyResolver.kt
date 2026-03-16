package com.ballrop.app.engine.spawn

import com.ballrop.app.model.game.BallMovementType
import com.ballrop.app.model.game.LevelConfig
import com.ballrop.app.model.game.SpawnPatternType

object DifficultyResolver {

    fun resolve(level: Int): LevelConfig {
        val safeLevel = level.coerceAtLeast(1)

        val targetCorrectCount = when {
            safeLevel <= 3 -> 18 + ((safeLevel - 1) * 2)
            safeLevel <= 8 -> 24 + ((safeLevel - 4) * 2)
            safeLevel <= 15 -> 34 + ((safeLevel - 9) * 2)
            else -> 48 + ((safeLevel - 16) * 2)
        }.coerceAtMost(72)

        val spawnIntervalMillis = when {
            safeLevel <= 4 -> 1220L - ((safeLevel - 1) * 18L)
            safeLevel <= 9 -> 1166L - ((safeLevel - 5) * 16L)
            safeLevel <= 14 -> 1086L - ((safeLevel - 10) * 14L)
            safeLevel <= 20 -> 1016L - ((safeLevel - 15) * 12L)
            else -> 944L - ((safeLevel - 21) * 8L)
        }.coerceAtLeast(760L)

        val baseFallSpeed = when {
            safeLevel <= 4 -> 228f + ((safeLevel - 1) * 8f)
            safeLevel <= 9 -> 260f + ((safeLevel - 5) * 7f)
            safeLevel <= 14 -> 295f + ((safeLevel - 10) * 6f)
            safeLevel <= 20 -> 325f + ((safeLevel - 15) * 5f)
            else -> 355f + ((safeLevel - 21) * 4f)
        }.coerceAtMost(420f)

        val movementTypes = when {
            safeLevel <= 2 -> listOf(
                BallMovementType.STRAIGHT
            )

            safeLevel <= 4 -> listOf(
                BallMovementType.STRAIGHT,
                BallMovementType.DRIFT_LEFT,
                BallMovementType.DRIFT_RIGHT
            )

            safeLevel <= 7 -> listOf(
                BallMovementType.STRAIGHT,
                BallMovementType.DRIFT_LEFT,
                BallMovementType.DRIFT_RIGHT,
                BallMovementType.ZIGZAG_SOFT
            )

            safeLevel <= 11 -> listOf(
                BallMovementType.STRAIGHT,
                BallMovementType.DRIFT_LEFT,
                BallMovementType.DRIFT_RIGHT,
                BallMovementType.ZIGZAG_SOFT,
                BallMovementType.DIAGONAL_LEFT,
                BallMovementType.DIAGONAL_RIGHT
            )

            else -> listOf(
                BallMovementType.STRAIGHT,
                BallMovementType.DRIFT_LEFT,
                BallMovementType.DRIFT_RIGHT,
                BallMovementType.ZIGZAG_SOFT,
                BallMovementType.ZIGZAG_HARD,
                BallMovementType.DIAGONAL_LEFT,
                BallMovementType.DIAGONAL_RIGHT
            )
        }

        val patternTypes = when {
            safeLevel <= 3 -> listOf(
                SpawnPatternType.SINGLE
            )

            safeLevel <= 5 -> listOf(
                SpawnPatternType.SINGLE,
                SpawnPatternType.COLUMN
            )

            safeLevel <= 8 -> listOf(
                SpawnPatternType.SINGLE,
                SpawnPatternType.COLUMN,
                SpawnPatternType.V_SHAPE
            )

            safeLevel <= 12 -> listOf(
                SpawnPatternType.SINGLE,
                SpawnPatternType.COLUMN,
                SpawnPatternType.V_SHAPE,
                SpawnPatternType.SQUARE
            )

            else -> listOf(
                SpawnPatternType.SINGLE,
                SpawnPatternType.COLUMN,
                SpawnPatternType.V_SHAPE,
                SpawnPatternType.SQUARE,
                SpawnPatternType.ARC
            )
        }

        val multiBallWaveChance = when {
            safeLevel <= 3 -> 0f
            safeLevel <= 5 -> 0.10f
            safeLevel <= 8 -> 0.16f
            safeLevel <= 12 -> 0.22f
            else -> 0.28f
        }

        val featuredPatternType = when {
            safeLevel in 5..7 -> SpawnPatternType.V_SHAPE
            safeLevel in 8..10 -> SpawnPatternType.SQUARE
            safeLevel in 11..14 -> SpawnPatternType.ARC
            safeLevel >= 15 -> {
                when ((safeLevel - 15) % 3) {
                    0 -> SpawnPatternType.V_SHAPE
                    1 -> SpawnPatternType.SQUARE
                    else -> SpawnPatternType.ARC
                }
            }

            else -> null
        }

        val featuredPatternChance = when {
            safeLevel < 5 -> 0f
            safeLevel <= 7 -> 0.38f
            safeLevel <= 10 -> 0.42f
            safeLevel <= 14 -> 0.46f
            else -> 0.50f
        }

        val bonusBallChance = when {
            safeLevel <= 2 -> 0f
            safeLevel <= 5 -> 0.06f
            safeLevel <= 10 -> 0.09f
            else -> 0.12f
        }

        val dangerBallChance = when {
            safeLevel <= 4 -> 0f
            safeLevel <= 8 -> 0.04f
            safeLevel <= 14 -> 0.07f
            else -> 0.10f
        }

        return LevelConfig(
            targetCorrectCount = targetCorrectCount,
            spawnIntervalMillis = spawnIntervalMillis,
            baseFallSpeed = baseFallSpeed,
            comboResetWindowMillis = 1700L.coerceAtLeast(1100L),
            maxComboBonusSteps = 4,
            horizontalDriftMultiplier = 1f,
            correctBaseScore = 10,
            wrongPenalty = 5,
            missPenalty = 5,

            introProgressThreshold = 0.28f,
            rushProgressThreshold = 0.78f,

            introSpawnMultiplier = 1.08f,
            introFallMultiplier = 0.94f,

            rushSpawnMultiplier = 0.93f,
            rushFallMultiplier = 1.08f,
            rushDriftMultiplier = 1.10f,

            bonusBallChance = bonusBallChance,
            dangerBallChance = dangerBallChance,
            bonusExtraScore = 15,
            dangerPenalty = 12,
            dangerEscapeScore = 8,

            allowedMovementTypes = movementTypes,
            zigzagAmplitude = when {
                safeLevel <= 6 -> 12f
                safeLevel <= 12 -> 18f
                else -> 24f
            },
            zigzagFrequency = when {
                safeLevel <= 6 -> 0.045f
                safeLevel <= 12 -> 0.060f
                else -> 0.075f
            },
            diagonalDriftStrength = when {
                safeLevel <= 8 -> 1.2f
                safeLevel <= 14 -> 1.6f
                else -> 2.0f
            },

            allowedPatternTypes = patternTypes,
            multiBallWaveChance = multiBallWaveChance,
            featuredPatternType = featuredPatternType,
            featuredPatternChance = featuredPatternChance
        )
    }
}
package com.ballrop.app.engine.spawn

import com.ballrop.app.model.game.BallMovementType
import com.ballrop.app.model.game.BasketMotionMode
import com.ballrop.app.model.game.LevelConfig
import com.ballrop.app.model.game.LevelFlowBlock
import com.ballrop.app.model.game.LevelFlowBlockType
import com.ballrop.app.model.game.LevelPartConfig
import com.ballrop.app.model.game.SpawnMixEntry
import com.ballrop.app.model.game.SpawnPatternType

object DifficultyResolver {

    private const val FINAL_DEFINED_LEVEL = 34

    fun isFinalLevel(level: Int): Boolean {
        return level.coerceAtLeast(1) >= FINAL_DEFINED_LEVEL
    }

    fun resolve(level: Int): LevelConfig {
        val safeLevel = level.coerceAtLeast(1).coerceAtMost(FINAL_DEFINED_LEVEL)

        val parts = when (safeLevel) {
            1 -> listOf(
                part(1, 20, 5, 2, BasketMotionMode.NONE, 1150L, 20, bb(100, 1 to 100)),
                part(2, 20, 5, 2, BasketMotionMode.NONE, 1150L, 35, bb(100, 1 to 100)),
                part(3, 20, 5, 2, BasketMotionMode.NONE, 1150L, 27, bb(100, 1 to 100))
            )

            2 -> listOf(
                part(1, 30, 5, 2, BasketMotionMode.NONE, 1150L, 30, bb(100, 1 to 95, 3 to 5)),
                part(2, 30, 5, 2, BasketMotionMode.NONE, 1150L, 45, bb(100, 1 to 95, 3 to 5)),
                part(3, 30, 5, 2, BasketMotionMode.NONE, 1150L, 37, bb(100, 1 to 88, 3 to 12))
            )

            3 -> listOf(
                part(1, 40, 5, 2, BasketMotionMode.NONE, 1150L, 30, bb(100, 1 to 90, 3 to 5, 2 to 5)),
                part(2, 40, 5, 2, BasketMotionMode.NONE, 1150L, 45, bb(100, 1 to 90, 3 to 5, 2 to 5)),
                part(3, 40, 5, 2, BasketMotionMode.NONE, 1150L, 37, bb(100, 1 to 80, 3 to 10, 2 to 10))
            )

            4 -> listOf(
                part(1, 40, 5, 2, BasketMotionMode.NONE, 1150L, 30, bb(100, 1 to 50, 3 to 5, 2 to 7, 4 to 38)),
                part(2, 40, 5, 2, BasketMotionMode.NONE, 1150L, 45, bb(100, 1 to 50, 3 to 5, 2 to 7, 4 to 38)),
                part(3, 40, 5, 2, BasketMotionMode.NONE, 1150L, 37, bb(100, 1 to 44, 3 to 5, 2 to 7, 4 to 44))
            )

            5 -> listOf(
                part(1, 40, 5, 2, BasketMotionMode.NONE, 1150L, 30, bb(100, 1 to 40, 3 to 5, 2 to 7, 4 to 20, 5 to 28)),
                part(2, 40, 5, 2, BasketMotionMode.NONE, 1150L, 45, bb(100, 1 to 40, 3 to 5, 2 to 7, 4 to 20, 5 to 28)),
                part(3, 40, 5, 2, BasketMotionMode.NONE, 1150L, 37, bb(100, 1 to 36, 3 to 5, 2 to 7, 4 to 20, 5 to 32))
            )

            6 -> listOf(
                part(1, 40, 5, 2, BasketMotionMode.NONE, 1150L, 35, bb(100, 1 to 36, 3 to 7, 2 to 7, 4 to 10, 5 to 20, 6 to 20)),
                part(2, 40, 5, 2, BasketMotionMode.NONE, 1150L, 50, bb(100, 1 to 36, 3 to 7, 2 to 7, 4 to 10, 5 to 20, 6 to 20)),
                part(3, 40, 5, 2, BasketMotionMode.NONE, 1150L, 42, bb(100, 1 to 28, 3 to 7, 2 to 7, 4 to 10, 5 to 20, 6 to 28))
            )

            7 -> listOf(
                part(1, 50, 5, 2, BasketMotionMode.NONE, 1150L, 35, bb(100, 1 to 28, 3 to 7, 2 to 7, 4 to 10, 5 to 5, 6 to 18, 7 to 25)),
                part(2, 50, 5, 2, BasketMotionMode.NONE, 1150L, 50, bb(100, 1 to 28, 3 to 7, 2 to 7, 4 to 10, 5 to 5, 6 to 18, 7 to 25)),
                part(3, 50, 5, 2, BasketMotionMode.NONE, 1150L, 42, bb(100, 1 to 31, 3 to 7, 2 to 7, 4 to 10, 5 to 5, 6 to 12, 7 to 28))
            )

            8 -> listOf(
                part(1, 50, 5, 2, BasketMotionMode.NONE, 1150L, 35, bb(100, 1 to 28, 3 to 10, 2 to 7, 4 to 10, 5 to 5, 6 to 11, 7 to 11, 8 to 18)),
                part(2, 50, 5, 2, BasketMotionMode.NONE, 1150L, 50, bb(100, 1 to 28, 3 to 10, 2 to 7, 4 to 10, 5 to 5, 6 to 11, 7 to 11, 8 to 18)),
                part(3, 50, 5, 2, BasketMotionMode.NONE, 1150L, 42, bb(100, 1 to 28, 3 to 10, 2 to 7, 4 to 10, 5 to 5, 6 to 11, 7 to 11, 8 to 18))
            )

            9 -> listOf(
                part(1, 50, 5, 2, BasketMotionMode.NONE, 1150L, 35, bb(100, 1 to 23, 3 to 10, 2 to 7, 4 to 17, 5 to 5, 6 to 9, 7 to 14, 8 to 5, 9 to 10)),
                part(2, 50, 5, 2, BasketMotionMode.NONE, 1150L, 50, bb(100, 1 to 28, 3 to 10, 2 to 7, 4 to 17, 5 to 5, 6 to 9, 7 to 14, 8 to 5, 9 to 5)),
                part(3, 50, 5, 2, BasketMotionMode.NONE, 1150L, 42, bb(100, 1 to 28, 3 to 10, 2 to 7, 4 to 17, 5 to 5, 6 to 9, 7 to 14, 8 to 5, 9 to 5))
            )

            10 -> listOf(
                part(1, 60, 5, 2, BasketMotionMode.NONE, 1150L, 35,
                    bb(30, 1 to 28, 3 to 10, 2 to 7, 4 to 22, 5 to 5, 6 to 9, 7 to 7, 8 to 7, 9 to 5),
                    s(10), g(3000),
                    rb(1 to 28, 3 to 10, 2 to 7, 4 to 22, 5 to 5, 6 to 9, 7 to 7, 8 to 7, 9 to 5)
                ),
                part(2, 60, 5, 2, BasketMotionMode.NONE, 1150L, 50,
                    bb(30, 1 to 28, 3 to 10, 2 to 7, 4 to 22, 5 to 5, 6 to 9, 7 to 7, 8 to 7, 9 to 5),
                    s(10), g(3000),
                    rb(1 to 28, 3 to 10, 2 to 7, 4 to 22, 5 to 5, 6 to 9, 7 to 7, 8 to 7, 9 to 5)
                ),
                part(3, 60, 5, 2, BasketMotionMode.NONE, 1150L, 42,
                    bb(30, 1 to 28, 3 to 10, 2 to 7, 4 to 22, 5 to 5, 6 to 9, 7 to 7, 8 to 7, 9 to 5),
                    s(10), g(3000),
                    bb(30, 1 to 28, 3 to 10, 2 to 7, 4 to 22, 5 to 5, 6 to 9, 7 to 7, 8 to 7, 9 to 5),
                    s(10), g(3000),
                    rb(1 to 28, 3 to 10, 2 to 7, 4 to 22, 5 to 5, 6 to 9, 7 to 7, 8 to 7, 9 to 5)
                )
            )

            11 -> listOf(
                part(1, 60, 5, 2, BasketMotionMode.NONE, 1150L, 35,
                    bb(30, 1 to 26, 3 to 10, 2 to 7, 4 to 21, 6 to 9, 7 to 7, 9 to 5, 11 to 15),
                    s(10), g(3000),
                    rb(1 to 26, 3 to 10, 2 to 7, 4 to 21, 6 to 9, 7 to 7, 9 to 5, 11 to 15)
                ),
                part(2, 60, 5, 2, BasketMotionMode.NONE, 1150L, 50,
                    bb(30, 1 to 26, 3 to 10, 2 to 7, 4 to 21, 6 to 9, 7 to 7, 9 to 5, 11 to 15),
                    s(10), g(3000),
                    rb(1 to 26, 3 to 10, 2 to 7, 4 to 21, 8 to 9, 5 to 7, 9 to 5, 11 to 15)
                ),
                part(3, 60, 5, 2, BasketMotionMode.NONE, 1150L, 42,
                    bb(30, 1 to 22, 3 to 10, 2 to 7, 4 to 18, 8 to 9, 5 to 7, 9 to 5, 11 to 22),
                    s(10), g(3000),
                    rb(1 to 22, 3 to 10, 2 to 7, 4 to 18, 8 to 9, 5 to 7, 9 to 5, 11 to 22)
                )
            )

            12 -> listOf(
                part(1, 60, 5, 2, BasketMotionMode.NONE, 1150L, 40,
                    bb(30, 1 to 27, 3 to 10, 2 to 7, 4 to 20, 5 to 5, 8 to 9, 7 to 9, 9 to 5, 11 to 8),
                    s(12), g(3000),
                    rb(1 to 32, 3 to 14, 2 to 7, 4 to 25, 8 to 9, 9 to 5, 11 to 8)
                ),
                part(2, 60, 5, 2, BasketMotionMode.NONE, 1150L, 55,
                    bb(30, 1 to 27, 3 to 10, 2 to 7, 4 to 20, 5 to 5, 8 to 9, 7 to 9, 9 to 5, 11 to 8),
                    s(12), g(3000),
                    rb(1 to 27, 3 to 14, 2 to 7, 4 to 20, 5 to 5, 7 to 5, 8 to 9, 9 to 5, 11 to 8)
                ),
                part(3, 60, 5, 2, BasketMotionMode.NONE, 1150L, 47,
                    bb(30, 1 to 27, 3 to 10, 2 to 7, 4 to 20, 5 to 5, 8 to 9, 7 to 9, 9 to 5, 11 to 8),
                    s(10), g(3000),
                    bb(30, 1 to 41, 2 to 2, 4 to 30, 5 to 5, 7 to 9, 9 to 5, 11 to 8),
                    s(12), g(3000),
                    rb(1 to 65, 4 to 35)
                )
            )

            13 -> listOf(
                part(1, 60, 5, 2, BasketMotionMode.NONE, 1150L, 40,
                    bb(100, 1 to 27, 3 to 6, 2 to 7, 4 to 20, 5 to 5, 8 to 5, 9 to 5, 11 to 5, 13 to 20)
                ),
                part(2, 60, 5, 2, BasketMotionMode.NONE, 1150L, 55,
                    bb(100, 1 to 27, 3 to 6, 2 to 7, 4 to 20, 7 to 5, 8 to 5, 9 to 5, 11 to 5, 13 to 20)
                ),
                part(3, 60, 5, 2, BasketMotionMode.NONE, 1150L, 47,
                    bb(100, 1 to 25, 3 to 10, 2 to 7, 4 to 18, 7 to 5, 8 to 5, 9 to 5, 11 to 5, 13 to 20)
                )
            )

            14 -> listOf(
                part(1, 60, 5, 2, BasketMotionMode.NONE, 1150L, 40,
                    bb(30, 1 to 31, 3 to 10, 2 to 10, 4 to 24, 7 to 5, 9 to 5, 11 to 5, 13 to 10),
                    s(14), g(3000),
                    rb(1 to 31, 3 to 10, 2 to 10, 4 to 24, 7 to 5, 9 to 5, 11 to 5, 13 to 10)
                ),
                part(2, 60, 5, 2, BasketMotionMode.NONE, 1150L, 55,
                    bb(30, 1 to 31, 3 to 10, 2 to 10, 4 to 19, 6 to 5, 9 to 5, 11 to 10, 13 to 10),
                    s(14), g(3000),
                    rb(1 to 31, 3 to 10, 2 to 10, 4 to 24, 7 to 5, 5 to 5, 11 to 5, 13 to 10)
                ),
                part(3, 60, 5, 2, BasketMotionMode.NONE, 1150L, 47,
                    bb(30, 1 to 55, 3 to 10, 2 to 10, 7 to 5, 9 to 5, 11 to 5, 13 to 10),
                    s(12), g(3000),
                    bb(30, 1 to 32, 3 to 10, 2 to 7, 4 to 25, 5 to 5, 6 to 9, 7 to 7, 9 to 5),
                    s(14), g(3000),
                    rb(1 to 28, 3 to 10, 2 to 7, 4 to 22, 5 to 5, 6 to 9, 7 to 7, 8 to 7, 9 to 5)
                )
            )

            15 -> listOf(
                part(1, 70, 5, 3, BasketMotionMode.NONE, 1150L, 40,
                    bb(100, 1 to 25, 3 to 10, 2 to 10, 4 to 24, 7 to 7, 9 to 8, 11 to 6, 13 to 10)
                ),
                part(2, 70, 5, 3, BasketMotionMode.NONE, 1150L, 55,
                    bb(100, 1 to 22, 3 to 10, 2 to 10, 4 to 21, 5 to 7, 6 to 6, 9 to 8, 11 to 6, 13 to 10)
                ),
                part(3, 70, 5, 3, BasketMotionMode.NONE, 1150L, 47,
                    bb(100, 1 to 22, 3 to 15, 2 to 10, 4 to 16, 5 to 7, 8 to 6, 9 to 8, 11 to 6, 13 to 10)
                )
            )

            16 -> listOf(
                part(1, 70, 5, 2, BasketMotionMode.NONE, 1150L, 40,
                    bb(30, 1 to 31, 3 to 10, 2 to 10, 4 to 24, 7 to 5, 9 to 5, 11 to 5, 13 to 10),
                    s(15), g(3000),
                    rb(1 to 31, 3 to 10, 2 to 10, 4 to 24, 5 to 5, 9 to 5, 11 to 5, 13 to 10)
                ),
                part(2, 70, 5, 2, BasketMotionMode.NONE, 1150L, 55,
                    bb(30, 1 to 36, 3 to 5, 2 to 10, 4 to 24, 7 to 5, 9 to 5, 11 to 5, 13 to 10),
                    s(15), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 29, 5 to 5, 11 to 5, 13 to 10)
                ),
                part(3, 70, 5, 2, BasketMotionMode.NONE, 1150L, 47,
                    bb(30, 1 to 40, 3 to 5, 2 to 7, 4 to 20, 5 to 28),
                    s(14), g(3000),
                    bb(30, 1 to 55, 3 to 10, 2 to 10, 7 to 5, 9 to 5, 11 to 5, 13 to 10),
                    s(15), g(3000),
                    rb(1 to 32, 3 to 10, 2 to 10, 4 to 22, 7 to 5, 9 to 5, 11 to 5, 13 to 10)
                )
            )

            17 -> listOf(
                part(1, 70, 5, 2, BasketMotionMode.NONE, 1150L, 40,
                    s(16), g(3000),
                    rb(1 to 31, 3 to 10, 2 to 10, 4 to 24, 7 to 5, 9 to 5, 11 to 5, 13 to 10)
                ),
                part(2, 70, 5, 2, BasketMotionMode.NONE, 1150L, 55,
                    bb(30, 1 to 46, 3 to 5, 2 to 10, 4 to 29, 9 to 5, 11 to 5),
                    s(16), g(3000),
                    rb(1 to 43, 3 to 8, 2 to 10, 4 to 29, 13 to 10)
                ),
                part(3, 70, 5, 2, BasketMotionMode.NONE, 1150L, 47,
                    bb(30, 1 to 70, 2 to 10, 3 to 5, 6 to 15),
                    s(15), g(3000),
                    bb(30, 1 to 40, 3 to 10, 4 to 15, 5 to 10, 8 to 15, 9 to 10),
                    s(16), g(3000),
                    rb(1 to 65, 2 to 5, 3 to 10, 5 to 20)
                )
            )

            18 -> listOf(
                part(1, 70, 5, 2, BasketMotionMode.NONE, 1150L, 45,
                    bb(30, 1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5),
                    s(17), g(3000),
                    rb(1 to 33, 3 to 5, 2 to 10, 4 to 23, 7 to 8, 8 to 5, 9 to 5, 11 to 5, 13 to 6)
                ),
                part(2, 70, 5, 2, BasketMotionMode.NONE, 1150L, 60,
                    bb(30, 1 to 31, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 10, 11 to 5),
                    s(17), g(3000),
                    rb(1 to 31, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 7 to 5, 9 to 10, 13 to 5)
                ),
                part(3, 70, 5, 2, BasketMotionMode.NONE, 1150L, 52,
                    bb(30, 1 to 46, 2 to 10, 3 to 5, 4 to 24, 6 to 15),
                    s(16), g(3000),
                    bb(30, 1 to 30, 2 to 10, 3 to 10, 4 to 20, 5 to 10, 9 to 10, 11 to 10),
                    s(17), g(3000),
                    rb(1 to 30, 2 to 10, 3 to 10, 4 to 20, 5 to 10, 6 to 10, 7 to 10)
                )
            )

            19 -> listOf(
                part(1, 70, 5, 2, BasketMotionMode.NONE, 1150L, 45,
                    bb(30, 1 to 33, 3 to 5, 2 to 10, 4 to 23, 7 to 8, 8 to 5, 9 to 5, 11 to 5, 13 to 6),
                    s(18), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(2, 70, 5, 2, BasketMotionMode.NONE, 1150L, 60,
                    bb(30, 1 to 33, 3 to 5, 2 to 10, 4 to 23, 5 to 8, 6 to 5, 9 to 5, 11 to 5, 13 to 6),
                    s(18), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 70, 5, 2, BasketMotionMode.NONE, 1150L, 52,
                    bb(30, 1 to 36, 2 to 10, 3 to 5, 4 to 24, 6 to 15, 13 to 10),
                    s(17), g(3000),
                    bb(30, 1 to 30, 2 to 10, 3 to 10, 4 to 20, 5 to 10, 9 to 10, 11 to 10),
                    s(18), g(3000),
                    rb(1 to 30, 2 to 10, 3 to 10, 4 to 20, 5 to 10, 6 to 10, 7 to 10)
                )
            )

            20 -> listOf(
                part(1, 80, 5, 2, BasketMotionMode.HORIZONTAL_SWAY, 1150L, 45,
                    bb(100, 1 to 26, 2 to 10, 3 to 10, 4 to 19, 5 to 5, 6 to 5, 7 to 5, 8 to 5, 9 to 5, 11 to 5, 13 to 5)
                ),
                part(2, 80, 5, 2, BasketMotionMode.HORIZONTAL_SWAY, 1150L, 60,
                    bb(100, 1 to 26, 2 to 10, 3 to 10, 4 to 19, 5 to 5, 6 to 5, 7 to 5, 8 to 5, 9 to 5, 11 to 5, 13 to 5)
                ),
                part(3, 80, 5, 2, BasketMotionMode.HORIZONTAL_SWAY, 1150L, 52,
                    bb(100, 1 to 20, 2 to 10, 3 to 10, 4 to 11, 5 to 7, 6 to 7, 7 to 7, 8 to 7, 9 to 7, 11 to 7, 13 to 7)
                )
            )

            21 -> listOf(
                part(1, 80, 5, 2, BasketMotionMode.NONE, 1150L, 45,
                    s(19), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(2, 80, 5, 2, BasketMotionMode.NONE, 1150L, 60,
                    bb(30, 1 to 33, 3 to 5, 2 to 10, 4 to 23, 5 to 8, 6 to 5, 9 to 5, 11 to 5, 13 to 6),
                    s(19), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 80, 5, 2, BasketMotionMode.NONE, 1150L, 52,
                    bb(30, 1 to 32, 2 to 10, 3 to 5, 4 to 20, 5 to 8, 6 to 15, 11 to 10),
                    s(18), g(3000),
                    bb(30, 1 to 32, 2 to 10, 3 to 5, 4 to 20, 5 to 8, 8 to 15, 13 to 10),
                    s(19), g(3000),
                    rb(1 to 32, 2 to 10, 3 to 5, 4 to 20, 7 to 8, 6 to 15, 11 to 10)
                )
            )

            22 -> listOf(
                part(1, 80, 5, 2, BasketMotionMode.NONE, 1150L, 45,
                    bb(30, 1 to 33, 3 to 5, 2 to 10, 4 to 23, 5 to 8, 6 to 5, 9 to 5, 11 to 5, 13 to 6),
                    s(20), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(2, 80, 5, 2, BasketMotionMode.NONE, 1150L, 60,
                    bb(30, 1 to 33, 3 to 5, 2 to 10, 4 to 23, 5 to 8, 6 to 5, 9 to 5, 11 to 5, 13 to 6),
                    s(20), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 80, 5, 2, BasketMotionMode.NONE, 1150L, 52,
                    s(19), g(3000),
                    s(20), g(3000),
                    rb(1 to 32, 2 to 10, 3 to 5, 4 to 20, 7 to 8, 6 to 15, 11 to 10)
                )
            )

            23 -> listOf(
                part(1, 80, 5, 2, BasketMotionMode.NONE, 1150L, 45,
                    bb(30, 1 to 33, 3 to 5, 2 to 10, 4 to 23, 5 to 8, 6 to 5, 9 to 5, 11 to 5, 13 to 6),
                    s(21), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 7 to 5, 9 to 5, 13 to 5)
                ),
                part(2, 80, 5, 2, BasketMotionMode.NONE, 1150L, 60,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(21), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 80, 5, 2, BasketMotionMode.NONE, 1150L, 52,
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 15, 11 to 15),
                    s(18), g(3000),
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 8 to 15, 13 to 15),
                    s(21), g(3000),
                    rb(1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 7, 7 to 8, 11 to 15)
                )
            )

            24 -> listOf(
                part(1, 80, 5, 2, BasketMotionMode.NONE, 1150L, 50,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(22), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(2, 80, 5, 2, BasketMotionMode.NONE, 1150L, 65,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(15), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 80, 5, 2, BasketMotionMode.NONE, 1150L, 57,
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 15, 11 to 7, 13 to 8),
                    s(12), g(3000),
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 5, 8 to 10, 13 to 15),
                    s(22), g(3000),
                    rb(1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 7, 7 to 8, 11 to 15)
                )
            )

            25 -> listOf(
                part(1, 90, 5, 2, BasketMotionMode.SWAP_EVERY_10_CORRECT, 1150L, 50,
                    bb(100, 1 to 20, 2 to 10, 3 to 10, 4 to 11, 5 to 7, 6 to 7, 7 to 7, 8 to 7, 9 to 7, 11 to 7, 13 to 7)
                ),
                part(2, 90, 5, 2, BasketMotionMode.SWAP_EVERY_10_CORRECT, 1150L, 65,
                    bb(100, 1 to 20, 2 to 10, 3 to 10, 4 to 11, 5 to 7, 6 to 7, 7 to 7, 8 to 7, 9 to 7, 11 to 7, 13 to 7)
                ),
                part(3, 90, 5, 2, BasketMotionMode.SWAP_EVERY_10_CORRECT, 1150L, 57,
                    bb(100, 1 to 26, 2 to 10, 3 to 15, 5 to 7, 6 to 7, 7 to 7, 8 to 7, 9 to 7, 11 to 7, 13 to 7)
                )
            )

            26 -> listOf(
                part(1, 90, 5, 2, BasketMotionMode.NONE, 1150L, 50,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(23), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(2, 90, 5, 2, BasketMotionMode.NONE, 1150L, 65,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(23), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 90, 5, 2, BasketMotionMode.NONE, 1150L, 57,
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 15, 11 to 7, 13 to 8),
                    s(22), g(3000),
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 5, 8 to 10, 13 to 15),
                    s(23), g(3000),
                    rb(1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 7, 7 to 8, 11 to 15)
                )
            )

            27 -> listOf(
                part(1, 90, 5, 2, BasketMotionMode.NONE, 1150L, 50,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 6 to 8, 8 to 5, 9 to 5, 11 to 6),
                    s(24), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 13 to 5)
                ),
                part(2, 90, 5, 2, BasketMotionMode.NONE, 1150L, 65,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(24), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 90, 5, 2, BasketMotionMode.NONE, 1150L, 57,
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 15, 11 to 7, 13 to 8),
                    s(23), g(3000),
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 5, 8 to 10, 13 to 15),
                    s(24), g(3000),
                    rb(1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 7, 7 to 8, 11 to 15)
                )
            )

            28 -> listOf(
                part(1, 90, 5, 2, BasketMotionMode.NONE, 1150L, 50,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(25), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(2, 90, 5, 2, BasketMotionMode.NONE, 1150L, 65,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(25), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 90, 5, 2, BasketMotionMode.NONE, 1150L, 57,
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 15, 11 to 7, 13 to 8),
                    s(24), g(3000),
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 5, 8 to 10, 13 to 15),
                    s(25), g(3000),
                    rb(1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 7, 7 to 8, 11 to 15)
                )
            )

            29 -> listOf(
                part(1, 90, 5, 2, BasketMotionMode.NONE, 1150L, 50,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 7 to 8, 6 to 5, 9 to 5, 13 to 6),
                    s(26), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(2, 90, 5, 2, BasketMotionMode.NONE, 1150L, 65,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(26), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 90, 5, 2, BasketMotionMode.NONE, 1150L, 57,
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 15, 11 to 7, 13 to 8),
                    s(21), g(3000),
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 5, 8 to 10, 13 to 15),
                    s(26), g(3000),
                    rb(1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 7, 7 to 8, 11 to 15)
                )
            )

            30 -> listOf(
                part(1, 100, 5, 2, BasketMotionMode.NONE, 1150L, 50,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 7 to 8, 8 to 5, 9 to 5, 11 to 6),
                    s(27), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(2, 100, 5, 2, BasketMotionMode.NONE, 1150L, 65,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(27), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 7 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 100, 5, 2, BasketMotionMode.NONE, 1150L, 57,
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 15, 11 to 7, 13 to 8),
                    s(20), g(3000),
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 5, 8 to 10, 13 to 15),
                    s(27), g(3000),
                    rb(1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 7, 7 to 8, 11 to 15)
                )
            )

            31 -> listOf(
                part(1, 100, 5, 3, BasketMotionMode.NONE, 1150L, 50,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 7 to 8, 6 to 5, 9 to 5, 13 to 6),
                    s(10), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(2, 100, 5, 3, BasketMotionMode.NONE, 1150L, 65,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(12), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 100, 5, 3, BasketMotionMode.NONE, 1150L, 57,
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 15, 11 to 7, 13 to 8),
                    s(14), g(3000),
                    bb(30, 1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 5, 8 to 10, 13 to 15),
                    s(15), g(3000),
                    rb(1 to 34, 2 to 10, 3 to 5, 4 to 21, 6 to 7, 7 to 8, 11 to 15)
                )
            )

            32 -> listOf(
                part(1, 110, 5, 2, BasketMotionMode.SWAP_EVERY_10_CORRECT, 1150L, 50,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 7 to 8, 6 to 5, 9 to 5, 13 to 6),
                    s(19), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(2, 110, 5, 2, BasketMotionMode.SWAP_EVERY_10_CORRECT, 1150L, 65,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(17), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 110, 5, 2, BasketMotionMode.SWAP_EVERY_10_CORRECT, 1150L, 57,
                    bb(30, 1 to 49, 2 to 10, 3 to 5, 4 to 36),
                    s(24), g(3000),
                    bb(30, 1 to 49, 2 to 10, 3 to 5, 4 to 36),
                    s(16), g(3000),
                    rb(1 to 49, 2 to 10, 3 to 5, 4 to 36)
                )
            )

            33 -> listOf(
                part(1, 110, 5, 2, BasketMotionMode.NONE, 1150L, 50,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 7 to 8, 6 to 5, 9 to 5, 13 to 6),
                    s(28), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(2, 110, 5, 2, BasketMotionMode.NONE, 1150L, 65,
                    bb(30, 1 to 33, 3 to 10, 2 to 10, 4 to 23, 5 to 8, 8 to 5, 9 to 5, 13 to 6),
                    s(23), g(3000),
                    rb(1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5)
                ),
                part(3, 110, 5, 2, BasketMotionMode.NONE, 1150L, 57,
                    bb(30, 1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 8 to 5, 9 to 5, 13 to 5),
                    s(25), g(3000),
                    bb(30, 1 to 36, 3 to 5, 2 to 10, 4 to 26, 5 to 8, 6 to 5, 9 to 5, 11 to 5),
                    s(28), g(3000),
                    rb(1 to 49, 2 to 10, 3 to 5, 4 to 36)
                )
            )

            34 -> listOf(
                part(1, 120, 5, 2, BasketMotionMode.NONE, 1150L, 50,
                    s(29), g(3000),
                    s(30), g(3000),
                    s(31), g(3000),
                    s(36), g(3000),
                    s(32), g(3000),
                    s(33), g(3000),
                    s(34), g(3000),
                    s(35), g(3000)
                )
            )

            else -> fallbackConfig(safeLevel)
        }

        val firstPart = parts.first()

        return LevelConfig(
            levelNumber = safeLevel,
            targetCorrectCount = firstPart.targetCorrectCount,
            spawnIntervalMillis = firstPart.spawnIntervalMillis,
            baseFallSpeed = fallSpeedPercentToBaseSpeed(firstPart.baseFallSpeedPercent),
            comboResetWindowMillis = 1800L,
            maxComboBonusSteps = 2,
            horizontalDriftMultiplier = 1f,
            correctBaseScore = 10,
            wrongPenalty = 5,
            missPenalty = 5,
            introProgressThreshold = 0.30f,
            rushProgressThreshold = 0.75f,
            introSpawnMultiplier = 1f,
            introFallMultiplier = 1f,
            rushSpawnMultiplier = 1f,
            rushFallMultiplier = 1f,
            rushDriftMultiplier = 1f,
            bonusBallChance = 0f,
            dangerBallChance = 0f,
            bonusExtraScore = 15,
            dangerPenalty = 12,
            dangerEscapeScore = 8,
            allowedMovementTypes = listOf(
                BallMovementType.STRAIGHT,
                BallMovementType.DRIFT_LEFT,
                BallMovementType.DRIFT_RIGHT,
                BallMovementType.ZIGZAG_SOFT,
                BallMovementType.ZIGZAG_HARD,
                BallMovementType.DIAGONAL_LEFT,
                BallMovementType.DIAGONAL_RIGHT
            ),
            zigzagAmplitude = 18f,
            zigzagFrequency = 0.05f,
            diagonalDriftStrength = 1.5f,
            allowedPatternTypes = listOf(
                SpawnPatternType.SINGLE,
                SpawnPatternType.COLUMN,
                SpawnPatternType.V_SHAPE,
                SpawnPatternType.SQUARE,
                SpawnPatternType.ARC
            ),
            multiBallWaveChance = 0f,
            featuredPatternType = null,
            featuredPatternChance = 0f,
            parts = parts
        )
    }

    private fun fallbackConfig(level: Int): List<LevelPartConfig> {
        val target = when {
            level <= 14 -> 60
            level <= 19 -> 70
            level <= 24 -> 80
            level <= 29 -> 90
            level <= 31 -> 100
            else -> 110
        }

        val p1Speed = when {
            level <= 11 -> 35
            level <= 17 -> 40
            level <= 23 -> 45
            else -> 50
        }

        val p2Speed = p1Speed + 15
        val p3Speed = p1Speed + 7

        return listOf(
            part(1, target, 5, 2, BasketMotionMode.NONE, 1150L, p1Speed, bb(100, 1 to 100)),
            part(2, target, 5, 2, BasketMotionMode.NONE, 1150L, p2Speed, bb(100, 1 to 100)),
            part(3, target, 5, 2, BasketMotionMode.NONE, 1150L, p3Speed, bb(100, 1 to 100))
        )
    }

    private fun fallSpeedPercentToBaseSpeed(percent: Int): Float {
        return 180f + (percent * 4.0f)
    }

    private fun part(
        partNumber: Int,
        target: Int,
        lives: Int,
        colors: Int,
        motion: BasketMotionMode,
        spawnInterval: Long,
        fallPercent: Int,
        vararg blocks: LevelFlowBlock
    ): LevelPartConfig {
        return LevelPartConfig(
            partNumber = partNumber,
            targetCorrectCount = target,
            maxMistakesAllowed = lives,
            colorCount = colors,
            basketMotionMode = motion,
            spawnIntervalMillis = spawnInterval,
            baseFallSpeedPercent = fallPercent,
            flowBlocks = blocks.toList()
        )
    }

    private fun bb(
        progressPercent: Int,
        vararg pairs: Pair<Int, Int>
    ): LevelFlowBlock {
        return LevelFlowBlock(
            type = LevelFlowBlockType.BALL_BLOCK,
            entries = pairs.map { SpawnMixEntry(it.first, it.second) },
            progressPercent = progressPercent
        )
    }

    private fun rb(
        vararg pairs: Pair<Int, Int>
    ): LevelFlowBlock {
        return LevelFlowBlock(
            type = LevelFlowBlockType.BALL_BLOCK,
            entries = pairs.map { SpawnMixEntry(it.first, it.second) },
            useRemainingProgress = true
        )
    }

    private fun s(shapeCode: Int): LevelFlowBlock {
        return LevelFlowBlock(
            type = LevelFlowBlockType.SHAPE_BLOCK,
            shapeCode = shapeCode
        )
    }

    private fun g(durationMillis: Long): LevelFlowBlock {
        return LevelFlowBlock(
            type = LevelFlowBlockType.GAP,
            gapDurationMillis = durationMillis
        )
    }
}
package com.ballrop.app.feature.game
import com.ballrop.app.model.game.GameSceneSnapshot
sealed interface GameAction {
    data object StartGame : GameAction
    data object FinishCountdown : GameAction
    data object ResetGame : GameAction
    data object Tick : GameAction
    data object NextLevel : GameAction

    data object PauseGame : GameAction
    data object ResumeGame : GameAction

    data class UpdateCanvasSize(
        val width: Float,
        val height: Float
    ) : GameAction

    data class PointerDown(
        val x: Float,
        val y: Float
    ) : GameAction

    data class PointerMove(
        val x: Float,
        val y: Float
    ) : GameAction

    data object PointerUp : GameAction

    data class ConsumeFeedback(
        val id: Long
    ) : GameAction

    data object TryAgain : GameAction

    data class StartFromLevel(
        val level: Int
    ) : GameAction

    data class StartFromProgress(
        val level: Int,
        val part: Int,
        val correctPlacedCount: Int,
        val missedCount: Int,
        val wrongCount: Int,
        val flowBlockIndex: Int,
        val flowBlockSpawnCount: Int,
        val flowShapeConsumed: Boolean,
        val flowShapePreGapCompleted: Boolean,
        val flowShapePostGapCompleted: Boolean
    ) : GameAction

    data class StartFromSceneSnapshot(
        val snapshot: GameSceneSnapshot
    ) : GameAction

    data object DebugAddLives : GameAction
    data object DebugAddCorrectCatch : GameAction
}
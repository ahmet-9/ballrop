package com.ballrop.app.feature.game

sealed interface GameAction {
    data object StartGame : GameAction
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
    data class StartFromLevel(val level: Int) : GameAction

    //DEBUG
    data object DebugAddLives : GameAction
    data object DebugAddCorrectCatch : GameAction
}

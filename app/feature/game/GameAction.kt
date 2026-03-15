package com.ballrop.app.feature.game

sealed interface GameAction {
    data object StartGame : GameAction
    data object ResetGame : GameAction
    data object Tick : GameAction
    data class UpdateCanvasSize(val width: Float, val height: Float) : GameAction
}
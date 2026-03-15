package com.ballrop.app.feature.game

import com.ballrop.app.model.game.BallItem
import com.ballrop.app.model.game.Basket
import com.ballrop.app.model.game.GameStatus
import com.ballrop.app.model.game.LevelConfig

data class GameUiState(
    val score: Int = 0,
    val correctPlacedCount: Int = 0,
    val missedCount: Int = 0,
    val wrongCount: Int = 0,
    val currentLevel: Int = 1,
    val status: GameStatus = GameStatus.READY,
    val levelConfig: LevelConfig = LevelConfig(),

    val canvasWidth: Float = 0f,
    val canvasHeight: Float = 0f,

    val baskets: List<Basket> = emptyList(),
    val balls: List<BallItem> = emptyList()
)
package com.ballrop.app.model.game

data class LevelConfig(
    val targetCorrectCount: Int = 20,
    val spawnIntervalMillis: Long = 1200L,
    val baseFallSpeed: Float = 240f
)
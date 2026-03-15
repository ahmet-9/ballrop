package com.ballrop.app.model.game

data class GameSessionSummary(
    val score: Int = 0,
    val reachedLevel: Int = 1,
    val totalCorrect: Int = 0,
    val totalMistakes: Int = 0,
    val bestCombo: Int = 0,
    val accuracyPercent: Int = 0,
    val durationSeconds: Int = 0
)

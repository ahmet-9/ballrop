package com.ballrop.app.feature.game

data class GameFeedbackPopup(
    val id: Long,
    val text: String,
    val x: Float,
    val y: Float,
    val alpha: Float = 1f,
    val scale: Float = 0.88f,
    val isPositive: Boolean,
    val isCombo: Boolean = false
)

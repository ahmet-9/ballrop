package com.ballrop.app.model.game

data class BallItem(
    val id: Long,
    val color: BallColor,
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float,
    val isDragging: Boolean = false
)
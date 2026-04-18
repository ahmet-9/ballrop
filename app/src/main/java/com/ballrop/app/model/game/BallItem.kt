package com.ballrop.app.model.game

data class BallItem(
    val id: Long,
    val color: BallColor,
    val kind: BallKind = BallKind.NORMAL,
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float,
    val driftX: Float = 0f,
    val velocityX: Float = 0f,
    val velocityY: Float = 0f,
    val rotationDeg: Float = 0f,
    val rotationSpeedDeg: Float = 0f,
    val scale: Float = 1f,
    val isDragging: Boolean = false,
    val justCaptured: Boolean = false,
    val movementType: BallMovementType = BallMovementType.STRAIGHT,
    val movementPhase: Float = 0f,
    val lockedToPattern: Boolean = false,
    val rimBounceCooldownFrames: Int = 0,
    val rimRejected: Boolean = false,

    // Yeni alanlar
    val spawnCode: Int = 1,
    val targetColor: BallColor = color,
    val colorRevealLocked: Boolean = true,
    val splitTriggered: Boolean = false
)
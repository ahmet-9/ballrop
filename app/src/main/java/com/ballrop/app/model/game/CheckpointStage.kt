package com.ballrop.app.model.game

data class CheckpointStage(
    val level: Int,
    val checkpointIndex: Int,
    val partIndex: Int
) {
    val isPart1: Boolean get() = partIndex == 1
    val isPart2: Boolean get() = partIndex == 2
    val isPart3: Boolean get() = partIndex == 3

    companion object {
        fun fromLevel(level: Int): CheckpointStage {
            val safeLevel = level.coerceIn(1, 48)
            val checkpointIndex = ((safeLevel - 1) / 3) + 1
            val partIndex = ((safeLevel - 1) % 3) + 1

            return CheckpointStage(
                level = safeLevel,
                checkpointIndex = checkpointIndex,
                partIndex = partIndex
            )
        }
    }
}
package com.ballrop.app.model.game

enum class BasketMotionMode {
    NONE,
    HORIZONTAL_SWAY,      // 51
    SWAP_EVERY_10_CORRECT // 52
}

enum class LevelFlowBlockType {
    BALL_BLOCK,
    SHAPE_BLOCK,
    GAP
}
val partType: PartType = PartType.BALL_THEN_SHAPE

enum class PartType {
    BALL_ONLY,
    BALL_THEN_SHAPE,
    SHAPE_ONLY,
    MIXED
}

data class SpawnMixEntry(
    val spawnCode: Int,
    val weightPercent: Int
)

data class LevelFlowBlock(
    val type: LevelFlowBlockType,
    val entries: List<SpawnMixEntry> = emptyList(),
    val shapeCode: Int? = null,
    val progressPercent: Int? = null, // örn 30 => hedef top sayısının yaklaşık %30'u
    val gapDurationMillis: Long = 0L,
    val useRemainingProgress: Boolean = false
)

data class LevelPartConfig(
    val partNumber: Int,
    val targetCorrectCount: Int,
    val maxMistakesAllowed: Int,
    val colorCount: Int,
    val basketMotionMode: BasketMotionMode,
    val spawnIntervalMillis: Long,
    val baseFallSpeedPercent: Int,
    val flowBlocks: List<LevelFlowBlock>
)
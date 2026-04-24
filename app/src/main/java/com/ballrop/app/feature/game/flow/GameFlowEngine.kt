package com.ballrop.app.feature.game.flow

import com.ballrop.app.model.game.LevelFlowBlock
import com.ballrop.app.model.game.LevelFlowBlockType
import com.ballrop.app.model.game.LevelPartConfig
import com.ballrop.app.model.game.SpawnMixEntry
import kotlin.math.ceil

sealed class GameFlowDirective {
    data class BallEntriesReady(val entries: List<SpawnMixEntry>) : GameFlowDirective()
    data class SpawnShape(val shapeCode: Int) : GameFlowDirective()
    data object Waiting : GameFlowDirective()
    data object Completed : GameFlowDirective()
}
data class GameFlowSnapshot(
    val currentBlockIndex: Int,
    val currentBlockSpawnCount: Int,
    val shapeConsumedForCurrentBlock: Boolean,
    val shapePreGapCompleted: Boolean,
    val shapePostGapCompleted: Boolean
)

class GameFlowEngine(
    private val partConfig: LevelPartConfig,
    snapshot: GameFlowSnapshot? = null
) {
    private val blocks: List<LevelFlowBlock> = if (partConfig.flowBlocks.isEmpty()) {
        listOf(
            LevelFlowBlock(
                type = LevelFlowBlockType.BALL_BLOCK,
                entries = listOf(SpawnMixEntry(1, 100)),
                progressPercent = 100
            )
        )
    } else {
        partConfig.flowBlocks
    }

    private val preShapeGapMillis = 3000L
    private val postShapeGapMillis = 6000L

    private var currentBlockIndex: Int = snapshot?.currentBlockIndex ?: 0
    private var currentBlockSpawnCount: Int = snapshot?.currentBlockSpawnCount ?: 0
    private var gapStartMillis: Long = 0L
    private var shapeConsumedForCurrentBlock: Boolean = snapshot?.shapeConsumedForCurrentBlock ?: false
    private var shapePreGapCompleted: Boolean = snapshot?.shapePreGapCompleted ?: false
    private var shapePostGapCompleted: Boolean = snapshot?.shapePostGapCompleted ?: false

    fun update(nowMillis: Long): GameFlowDirective {
        while (currentBlockIndex < blocks.size) {
            val block = blocks[currentBlockIndex]

            when (block.type) {
                LevelFlowBlockType.BALL_BLOCK -> {
                    if (shouldAdvanceCurrentBallBlock()) {
                        advanceToNextBlock()
                        continue
                    }

                    val entries = if (block.entries.isEmpty()) {
                        listOf(SpawnMixEntry(1, 100))
                    } else {
                        block.entries
                    }

                    return GameFlowDirective.BallEntriesReady(entries)
                }

                LevelFlowBlockType.SHAPE_BLOCK -> {
                    if (!shapePreGapCompleted) {
                        if (gapStartMillis == 0L) {
                            gapStartMillis = nowMillis
                        }

                        val elapsed = nowMillis - gapStartMillis
                        if (elapsed < preShapeGapMillis) {
                            return GameFlowDirective.Waiting
                        }

                        shapePreGapCompleted = true
                        gapStartMillis = 0L
                    }

                    if (!shapeConsumedForCurrentBlock) {
                        shapeConsumedForCurrentBlock = true
                        return GameFlowDirective.SpawnShape(block.shapeCode ?: 0)
                    }

                    if (!shapePostGapCompleted) {
                        val nextBlock = blocks.getOrNull(currentBlockIndex + 1)

                        if (nextBlock?.type == LevelFlowBlockType.GAP) {
                            advanceToNextBlock()
                            continue
                        }

                        if (gapStartMillis == 0L) {
                            gapStartMillis = nowMillis
                        }

                        val elapsed = nowMillis - gapStartMillis
                        if (elapsed < postShapeGapMillis) {
                            return GameFlowDirective.Waiting
                        }

                        shapePostGapCompleted = true
                        gapStartMillis = 0L
                    }

                    advanceToNextBlock()
                    continue
                }

                LevelFlowBlockType.GAP -> {
                    if (gapStartMillis == 0L) {
                        gapStartMillis = nowMillis
                    }

                    val effectiveGap = if (isGapAfterShape()) {
                        postShapeGapMillis
                    } else {
                        block.gapDurationMillis
                    }

                    val elapsed = nowMillis - gapStartMillis
                    if (elapsed >= effectiveGap) {
                        advanceToNextBlock()
                        continue
                    }

                    return GameFlowDirective.Waiting
                }
            }
        }

        return GameFlowDirective.Completed
    }

    fun onBallSpawned() {
        val block = currentBlock() ?: return
        if (block.type != LevelFlowBlockType.BALL_BLOCK) return
        currentBlockSpawnCount += 1
    }
    fun exportSnapshot(): GameFlowSnapshot {
        return GameFlowSnapshot(
            currentBlockIndex = currentBlockIndex,
            currentBlockSpawnCount = currentBlockSpawnCount,
            shapeConsumedForCurrentBlock = shapeConsumedForCurrentBlock,
            shapePreGapCompleted = shapePreGapCompleted,
            shapePostGapCompleted = shapePostGapCompleted
        )
    }

    private fun currentBlock(): LevelFlowBlock? {
        return blocks.getOrNull(currentBlockIndex)
    }

    private fun advanceToNextBlock() {
        currentBlockIndex += 1
        currentBlockSpawnCount = 0
        gapStartMillis = 0L
        shapeConsumedForCurrentBlock = false
        shapePreGapCompleted = false
        shapePostGapCompleted = false
    }

    private fun shouldAdvanceCurrentBallBlock(): Boolean {
        val block = currentBlock() ?: return false
        if (block.type != LevelFlowBlockType.BALL_BLOCK) return false

        if (block.useRemainingProgress) return false
        if (!hasMeaningfulBlockAfterCurrent()) return false

        val quota = approximateSpawnQuota(block)
        return currentBlockSpawnCount >= quota
    }

    private fun hasMeaningfulBlockAfterCurrent(): Boolean {
        return blocks
            .drop(currentBlockIndex + 1)
            .any { it.type == LevelFlowBlockType.BALL_BLOCK || it.type == LevelFlowBlockType.SHAPE_BLOCK }
    }

    private fun isGapAfterShape(): Boolean {
        if (currentBlock()?.type != LevelFlowBlockType.GAP) return false
        val previousBlock = blocks.getOrNull(currentBlockIndex - 1)
        return previousBlock?.type == LevelFlowBlockType.SHAPE_BLOCK
    }

    private fun approximateSpawnQuota(block: LevelFlowBlock): Int {
        val target = partConfig.targetCorrectCount.coerceAtLeast(1)
        val percent = (block.progressPercent ?: 100).coerceIn(1, 100)

        return ceil(target * (percent / 100f))
            .toInt()
            .coerceAtLeast(6)
    }
}
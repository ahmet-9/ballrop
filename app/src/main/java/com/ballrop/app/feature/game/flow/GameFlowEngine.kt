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

class GameFlowEngine(
    private val partConfig: LevelPartConfig
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

    private var currentBlockIndex: Int = 0
    private var currentBlockSpawnCount: Int = 0
    private var gapStartMillis: Long = 0L
    private var shapeConsumedForCurrentBlock: Boolean = false

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
                    if (!shapeConsumedForCurrentBlock) {
                        shapeConsumedForCurrentBlock = true
                        return GameFlowDirective.SpawnShape(block.shapeCode ?: 0)
                    }

                    advanceToNextBlock()
                    continue
                }

                LevelFlowBlockType.GAP -> {
                    if (gapStartMillis == 0L) {
                        gapStartMillis = nowMillis
                    }

                    val elapsed = nowMillis - gapStartMillis
                    if (elapsed >= block.gapDurationMillis) {
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

    private fun currentBlock(): LevelFlowBlock? {
        return blocks.getOrNull(currentBlockIndex)
    }

    private fun advanceToNextBlock() {
        currentBlockIndex += 1
        currentBlockSpawnCount = 0
        gapStartMillis = 0L
        shapeConsumedForCurrentBlock = false
    }

    private fun shouldAdvanceCurrentBallBlock(): Boolean {
        val block = currentBlock() ?: return false
        if (block.type != LevelFlowBlockType.BALL_BLOCK) return false

        // "kalan kısım" olan blok part bitene kadar sürer.
        // Part bitişini GameController yönetir.
        if (block.useRemainingProgress) return false

        // Son meaningful ball block ise ve sonrası yoksa, part bitene kadar sürsün.
        if (!hasMeaningfulBlockAfterCurrent()) return false

        val quota = approximateSpawnQuota(block)
        return currentBlockSpawnCount >= quota
    }

    private fun hasMeaningfulBlockAfterCurrent(): Boolean {
        return blocks
            .drop(currentBlockIndex + 1)
            .any { it.type == LevelFlowBlockType.BALL_BLOCK || it.type == LevelFlowBlockType.SHAPE_BLOCK }
    }

    private fun approximateSpawnQuota(block: LevelFlowBlock): Int {
        val target = partConfig.targetCorrectCount.coerceAtLeast(1)
        val percent = (block.progressPercent ?: 100).coerceIn(1, 100)

        // Dökümandaki akış sırasına daha sadık olmak için
        // yüzdeyi doğrudan hedef top sayısına bağlıyoruz.
        // Bu quota "doğru top" değil "spawn sayısı" bazlıdır.
        return ceil(target * (percent / 100f))
            .toInt()
            .coerceAtLeast(6)
    }
}
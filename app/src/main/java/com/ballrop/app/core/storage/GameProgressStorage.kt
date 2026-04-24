package com.ballrop.app.core.storage

import android.content.Context
import android.content.SharedPreferences

class GameProgressStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveIfBetter(score: Int, level: Int) {
        prefs.edit()
            .putInt(KEY_BEST_SCORE, maxOf(score, getBestScore()))
            .putInt(KEY_BEST_LEVEL, maxOf(level, getBestLevel()))
            .apply()
    }

    fun saveCheckpointStartLevel(level: Int) {
        prefs.edit()
            .putInt(KEY_CHECKPOINT_START_LEVEL, level.coerceAtLeast(1))
            .putBoolean(KEY_CAN_CONTINUE, level > 1 || hasResumeProgress())
            .apply()
    }

    fun saveResumeProgress(
        level: Int,
        part: Int,
        correctPlacedCount: Int,
        missedCount: Int,
        wrongCount: Int,
        maxMistakesAllowed: Int,
        targetCorrectCount: Int,
        flowBlockIndex: Int,
        flowBlockSpawnCount: Int,
        flowShapeConsumed: Boolean,
        flowShapePreGapCompleted: Boolean,
        flowShapePostGapCompleted: Boolean
    ) {
        prefs.edit()
            .putInt(KEY_RESUME_LEVEL, level.coerceAtLeast(1))
            .putInt(KEY_RESUME_PART, part.coerceIn(1, 3))
            .putInt(KEY_RESUME_CORRECT_COUNT, correctPlacedCount.coerceAtLeast(0))
            .putInt(KEY_RESUME_MISSED_COUNT, missedCount.coerceAtLeast(0))
            .putInt(KEY_RESUME_WRONG_COUNT, wrongCount.coerceAtLeast(0))
            .putInt(KEY_RESUME_MAX_MISTAKES, maxMistakesAllowed.coerceAtLeast(1))
            .putInt(KEY_RESUME_TARGET_CORRECT_COUNT, targetCorrectCount.coerceAtLeast(1))
            .putInt(KEY_RESUME_FLOW_BLOCK_INDEX, flowBlockIndex.coerceAtLeast(0))
            .putInt(KEY_RESUME_FLOW_BLOCK_SPAWN_COUNT, flowBlockSpawnCount.coerceAtLeast(0))
            .putBoolean(KEY_HAS_RESUME_PROGRESS, true)
            .putBoolean(KEY_CAN_CONTINUE, true)
            .putBoolean(KEY_RESUME_FLOW_SHAPE_CONSUMED, flowShapeConsumed)
            .putBoolean(KEY_RESUME_FLOW_SHAPE_PRE_GAP_COMPLETED, flowShapePreGapCompleted)
            .putBoolean(KEY_RESUME_FLOW_SHAPE_POST_GAP_COMPLETED, flowShapePostGapCompleted)
            .apply()
    }

    fun clearResumeProgress() {
        prefs.edit()
            .putBoolean(KEY_HAS_RESUME_PROGRESS, false)
            .remove(KEY_RESUME_LEVEL)
            .remove(KEY_RESUME_PART)
            .remove(KEY_RESUME_CORRECT_COUNT)
            .remove(KEY_RESUME_MISSED_COUNT)
            .remove(KEY_RESUME_WRONG_COUNT)
            .remove(KEY_RESUME_MAX_MISTAKES)
            .remove(KEY_RESUME_TARGET_CORRECT_COUNT)
            .remove(KEY_RESUME_FLOW_BLOCK_INDEX)
            .remove(KEY_RESUME_FLOW_BLOCK_SPAWN_COUNT)
            .remove(KEY_RESUME_FLOW_SHAPE_CONSUMED)
            .remove(KEY_RESUME_FLOW_SHAPE_PRE_GAP_COMPLETED)
            .remove(KEY_RESUME_FLOW_SHAPE_POST_GAP_COMPLETED)
            .putBoolean(KEY_CAN_CONTINUE, getCheckpointStartLevel() > 1)
            .apply()
    }

    fun clearContinueProgress() {
        prefs.edit()
            .putInt(KEY_CHECKPOINT_START_LEVEL, 1)
            .putBoolean(KEY_HAS_RESUME_PROGRESS, false)
            .putBoolean(KEY_CAN_CONTINUE, false)
            .remove(KEY_RESUME_LEVEL)
            .remove(KEY_RESUME_PART)
            .remove(KEY_RESUME_CORRECT_COUNT)
            .remove(KEY_RESUME_MISSED_COUNT)
            .remove(KEY_RESUME_WRONG_COUNT)
            .remove(KEY_RESUME_MAX_MISTAKES)
            .remove(KEY_RESUME_TARGET_CORRECT_COUNT)
            .remove(KEY_RESUME_FLOW_BLOCK_INDEX)
            .remove(KEY_RESUME_FLOW_BLOCK_SPAWN_COUNT)
            .remove(KEY_RESUME_FLOW_SHAPE_CONSUMED)
            .remove(KEY_RESUME_FLOW_SHAPE_PRE_GAP_COMPLETED)
            .remove(KEY_RESUME_FLOW_SHAPE_POST_GAP_COMPLETED)
            .apply()
    }

    fun getBestScore(): Int = prefs.getInt(KEY_BEST_SCORE, 0)

    fun getBestLevel(): Int = prefs.getInt(KEY_BEST_LEVEL, 1)

    fun getCheckpointStartLevel(): Int =
        prefs.getInt(KEY_CHECKPOINT_START_LEVEL, 1).coerceAtLeast(1)

    fun hasResumeProgress(): Boolean =
        prefs.getBoolean(KEY_HAS_RESUME_PROGRESS, false)

    fun getResumeLevel(): Int =
        prefs.getInt(KEY_RESUME_LEVEL, getCheckpointStartLevel()).coerceAtLeast(1)

    fun getResumePart(): Int =
        prefs.getInt(KEY_RESUME_PART, 1).coerceIn(1, 3)

    fun getResumeCorrectPlacedCount(): Int =
        prefs.getInt(KEY_RESUME_CORRECT_COUNT, 0).coerceAtLeast(0)

    fun getResumeMissedCount(): Int =
        prefs.getInt(KEY_RESUME_MISSED_COUNT, 0).coerceAtLeast(0)

    fun getResumeWrongCount(): Int =
        prefs.getInt(KEY_RESUME_WRONG_COUNT, 0).coerceAtLeast(0)

    fun getResumeMaxMistakesAllowed(): Int =
        prefs.getInt(KEY_RESUME_MAX_MISTAKES, 5).coerceAtLeast(1)

    fun getResumeFlowBlockIndex(): Int =
        prefs.getInt(KEY_RESUME_FLOW_BLOCK_INDEX, 0).coerceAtLeast(0)

    fun getResumeFlowBlockSpawnCount(): Int =
        prefs.getInt(KEY_RESUME_FLOW_BLOCK_SPAWN_COUNT, 0).coerceAtLeast(0)

    fun getResumeFlowShapeConsumed(): Boolean =
        prefs.getBoolean(KEY_RESUME_FLOW_SHAPE_CONSUMED, false)

    fun getResumeFlowShapePreGapCompleted(): Boolean =
        prefs.getBoolean(KEY_RESUME_FLOW_SHAPE_PRE_GAP_COMPLETED, false)

    fun getResumeFlowShapePostGapCompleted(): Boolean =
        prefs.getBoolean(KEY_RESUME_FLOW_SHAPE_POST_GAP_COMPLETED, false)

    fun getResumeTargetCorrectCount(): Int =
        prefs.getInt(KEY_RESUME_TARGET_CORRECT_COUNT, 20).coerceAtLeast(1)

    fun getResumeRemainingTargetCount(): Int {
        val target = getResumeTargetCorrectCount()
        val correct = getResumeCorrectPlacedCount()
        return (target - correct).coerceAtLeast(0)
    }

    fun getCheckpointPart(): Int =
        if (hasResumeProgress()) getResumePart() else 1

    fun getCheckpointLives(): Int {
        if (!hasResumeProgress()) return 5

        val remaining = getResumeMaxMistakesAllowed() - getResumeMissedCount() - getResumeWrongCount()
        return remaining.coerceIn(0, getResumeMaxMistakesAllowed())
    }

    fun getContinueLevel(): Int =
        if (hasResumeProgress()) getResumeLevel() else getCheckpointStartLevel()

    fun canContinue(): Boolean =
        hasResumeProgress() || getCheckpointStartLevel() > 1 || prefs.getBoolean(KEY_CAN_CONTINUE, false)

    companion object {
        private const val PREF_NAME = "ballrop_game_progress"

        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_BEST_LEVEL = "best_level"

        private const val KEY_CHECKPOINT_START_LEVEL = "checkpoint_start_level"
        private const val KEY_CAN_CONTINUE = "can_continue"
        private const val KEY_RESUME_FLOW_BLOCK_INDEX = "resume_flow_block_index"
        private const val KEY_RESUME_FLOW_BLOCK_SPAWN_COUNT = "resume_flow_block_spawn_count"
        private const val KEY_RESUME_FLOW_SHAPE_CONSUMED = "resume_flow_shape_consumed"
        private const val KEY_RESUME_FLOW_SHAPE_PRE_GAP_COMPLETED = "resume_flow_shape_pre_gap_completed"
        private const val KEY_RESUME_FLOW_SHAPE_POST_GAP_COMPLETED = "resume_flow_shape_post_gap_completed"
        private const val KEY_HAS_RESUME_PROGRESS = "has_resume_progress"
        private const val KEY_RESUME_LEVEL = "resume_level"
        private const val KEY_RESUME_PART = "resume_part"
        private const val KEY_RESUME_CORRECT_COUNT = "resume_correct_count"
        private const val KEY_RESUME_MISSED_COUNT = "resume_missed_count"
        private const val KEY_RESUME_WRONG_COUNT = "resume_wrong_count"
        private const val KEY_RESUME_MAX_MISTAKES = "resume_max_mistakes"
        private const val KEY_RESUME_TARGET_CORRECT_COUNT = "resume_target_correct_count"
    }
}
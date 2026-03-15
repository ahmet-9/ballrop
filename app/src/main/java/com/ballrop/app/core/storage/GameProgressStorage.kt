package com.ballrop.app.core.storage

import android.content.Context
import android.content.SharedPreferences

class GameProgressStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveIfBetter(score: Int, level: Int) {
        val bestScore = getBestScore()
        val bestLevel = getBestLevel()

        prefs.edit()
            .putInt(KEY_BEST_SCORE, maxOf(score, bestScore))
            .putInt(KEY_BEST_LEVEL, maxOf(level, bestLevel))
            .apply()
    }

    fun saveCheckpointStartLevel(level: Int) {
        prefs.edit()
            .putInt(KEY_CHECKPOINT_START_LEVEL, level.coerceAtLeast(1))
            .putBoolean(KEY_CAN_CONTINUE, level > 1)
            .apply()
    }

    fun clearContinueProgress() {
        prefs.edit()
            .putInt(KEY_CHECKPOINT_START_LEVEL, 1)
            .putBoolean(KEY_CAN_CONTINUE, false)
            .apply()
    }

    fun getBestScore(): Int = prefs.getInt(KEY_BEST_SCORE, 0)

    fun getBestLevel(): Int = prefs.getInt(KEY_BEST_LEVEL, 1)

    fun getCheckpointStartLevel(): Int = prefs.getInt(KEY_CHECKPOINT_START_LEVEL, 1)

    fun canContinue(): Boolean = prefs.getBoolean(KEY_CAN_CONTINUE, false)

    companion object {
        private const val PREF_NAME = "ballrop_game_progress"
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_BEST_LEVEL = "best_level"
        private const val KEY_CHECKPOINT_START_LEVEL = "checkpoint_start_level"
        private const val KEY_CAN_CONTINUE = "can_continue"
    }
}
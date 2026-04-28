package com.ballrop.app.core.storage

import android.content.Context
import android.content.SharedPreferences

import com.ballrop.app.model.game.BallColor
import com.ballrop.app.model.game.BallItem
import com.ballrop.app.model.game.BallKind
import com.ballrop.app.model.game.BallMovementType
import com.ballrop.app.model.game.Basket
import com.ballrop.app.model.game.GameSceneSnapshot
import org.json.JSONArray
import org.json.JSONObject

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

    fun saveResumeSceneSnapshot(snapshot: GameSceneSnapshot) {
        prefs.edit()
            .putString(KEY_RESUME_SCENE_SNAPSHOT, encodeSceneSnapshot(snapshot))
            .putBoolean(KEY_HAS_RESUME_PROGRESS, true)
            .putBoolean(KEY_CAN_CONTINUE, true)
            .apply()
    }

    fun getResumeSceneSnapshot(): GameSceneSnapshot? {
        val raw = prefs.getString(KEY_RESUME_SCENE_SNAPSHOT, null) ?: return null
        return runCatching {
            decodeSceneSnapshot(raw)
        }.getOrNull()
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
            .remove(KEY_RESUME_SCENE_SNAPSHOT)
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
            .remove(KEY_RESUME_SCENE_SNAPSHOT)
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




    private fun encodeSceneSnapshot(snapshot: GameSceneSnapshot): String {
        return JSONObject()
            .put("level", snapshot.level)
            .put("part", snapshot.part)
            .put("score", snapshot.score)
            .put("correctPlacedCount", snapshot.correctPlacedCount)
            .put("totalCorrectCount", snapshot.totalCorrectCount)
            .put("missedCount", snapshot.missedCount)
            .put("wrongCount", snapshot.wrongCount)
            .put("comboCount", snapshot.comboCount)
            .put("bestCombo", snapshot.bestCombo)
            .put("maxMistakesAllowed", snapshot.maxMistakesAllowed)
            .put("targetCorrectCount", snapshot.targetCorrectCount)
            .put("canvasWidth", snapshot.canvasWidth.toDouble())
            .put("canvasHeight", snapshot.canvasHeight.toDouble())
            .put("balls", JSONArray().apply {
                snapshot.balls.forEach { put(encodeBall(it)) }
            })
            .put("baskets", JSONArray().apply {
                snapshot.baskets.forEach { put(encodeBasket(it)) }
            })
            .put("activeShapeBallIds", JSONArray().apply {
                snapshot.activeShapeBallIds.forEach { put(it) }
            })
            .put("shapeRemainingCount", snapshot.shapeRemainingCount)
            .put("shapeTotalCount", snapshot.shapeTotalCount)
            .put("isShapeActive", snapshot.isShapeActive)
            .put("nextBallId", snapshot.nextBallId)
            .put("nextPopupId", snapshot.nextPopupId)
            .put("nextFeedbackEventId", snapshot.nextFeedbackEventId)
            .put("lastSpawnAgeMillis", snapshot.lastSpawnAgeMillis)
            .put("lastCorrectCatchAgeMillis", snapshot.lastCorrectCatchAgeMillis)
            .put("slowMotionRemainingMillis", snapshot.slowMotionRemainingMillis)
            .put("shapeCooldownRemainingMillis", snapshot.shapeCooldownRemainingMillis)
            .put("flowBlockIndex", snapshot.flowBlockIndex)
            .put("flowBlockSpawnCount", snapshot.flowBlockSpawnCount)
            .put("flowShapeConsumed", snapshot.flowShapeConsumed)
            .put("flowShapePreGapCompleted", snapshot.flowShapePreGapCompleted)
            .put("flowShapePostGapCompleted", snapshot.flowShapePostGapCompleted)
            .put("basketSwayOffset", snapshot.basketSwayOffset.toDouble())
            .put("basketSwayDirection", snapshot.basketSwayDirection.toDouble())
            .put("basketSwapState", snapshot.basketSwapState)
            .put("lastSwapThreshold", snapshot.lastSwapThreshold)
            .toString()
    }

    private fun decodeSceneSnapshot(raw: String): GameSceneSnapshot {
        val json = JSONObject(raw)

        return GameSceneSnapshot(
            level = json.getInt("level"),
            part = json.getInt("part"),
            score = json.getInt("score"),
            correctPlacedCount = json.getInt("correctPlacedCount"),
            totalCorrectCount = json.getInt("totalCorrectCount"),
            missedCount = json.getInt("missedCount"),
            wrongCount = json.getInt("wrongCount"),
            comboCount = json.getInt("comboCount"),
            bestCombo = json.getInt("bestCombo"),
            maxMistakesAllowed = json.getInt("maxMistakesAllowed"),
            targetCorrectCount = json.getInt("targetCorrectCount"),
            canvasWidth = json.getDouble("canvasWidth").toFloat(),
            canvasHeight = json.getDouble("canvasHeight").toFloat(),
            balls = decodeBalls(json.getJSONArray("balls")),
            baskets = decodeBaskets(json.getJSONArray("baskets")),
            activeShapeBallIds = decodeLongSet(json.getJSONArray("activeShapeBallIds")),
            shapeRemainingCount = json.getInt("shapeRemainingCount"),
            shapeTotalCount = json.getInt("shapeTotalCount"),
            isShapeActive = json.getBoolean("isShapeActive"),
            nextBallId = json.getLong("nextBallId"),
            nextPopupId = json.getLong("nextPopupId"),
            nextFeedbackEventId = json.getLong("nextFeedbackEventId"),
            lastSpawnAgeMillis = json.getLong("lastSpawnAgeMillis"),
            lastCorrectCatchAgeMillis = json.getLong("lastCorrectCatchAgeMillis"),
            slowMotionRemainingMillis = json.getLong("slowMotionRemainingMillis"),
            shapeCooldownRemainingMillis = json.getLong("shapeCooldownRemainingMillis"),
            flowBlockIndex = json.getInt("flowBlockIndex"),
            flowBlockSpawnCount = json.getInt("flowBlockSpawnCount"),
            flowShapeConsumed = json.getBoolean("flowShapeConsumed"),
            flowShapePreGapCompleted = json.getBoolean("flowShapePreGapCompleted"),
            flowShapePostGapCompleted = json.getBoolean("flowShapePostGapCompleted"),
            basketSwayOffset = json.getDouble("basketSwayOffset").toFloat(),
            basketSwayDirection = json.getDouble("basketSwayDirection").toFloat(),
            basketSwapState = json.getBoolean("basketSwapState"),
            lastSwapThreshold = json.getInt("lastSwapThreshold")
        )
    }

    private fun encodeBall(ball: BallItem): JSONObject {
        return JSONObject()
            .put("id", ball.id)
            .put("color", ball.color.name)
            .put("kind", ball.kind.name)
            .put("x", ball.x.toDouble())
            .put("y", ball.y.toDouble())
            .put("radius", ball.radius.toDouble())
            .put("speed", ball.speed.toDouble())
            .put("driftX", ball.driftX.toDouble())
            .put("velocityX", ball.velocityX.toDouble())
            .put("velocityY", ball.velocityY.toDouble())
            .put("rotationDeg", ball.rotationDeg.toDouble())
            .put("rotationSpeedDeg", ball.rotationSpeedDeg.toDouble())
            .put("scale", ball.scale.toDouble())
            .put("justCaptured", ball.justCaptured)
            .put("movementType", ball.movementType.name)
            .put("movementPhase", ball.movementPhase.toDouble())
            .put("lockedToPattern", ball.lockedToPattern)
            .put("rimBounceCooldownFrames", ball.rimBounceCooldownFrames)
            .put("rimRejected", ball.rimRejected)
            .put("spawnCode", ball.spawnCode)
            .put("targetColor", ball.targetColor.name)
            .put("colorRevealLocked", ball.colorRevealLocked)
            .put("splitTriggered", ball.splitTriggered)
    }

    private fun decodeBalls(array: JSONArray): List<BallItem> {
        return List(array.length()) { index ->
            val json = array.getJSONObject(index)

            BallItem(
                id = json.getLong("id"),
                color = BallColor.valueOf(json.getString("color")),
                kind = BallKind.valueOf(json.getString("kind")),
                x = json.getDouble("x").toFloat(),
                y = json.getDouble("y").toFloat(),
                radius = json.getDouble("radius").toFloat(),
                speed = json.getDouble("speed").toFloat(),
                driftX = json.getDouble("driftX").toFloat(),
                velocityX = json.getDouble("velocityX").toFloat(),
                velocityY = json.getDouble("velocityY").toFloat(),
                rotationDeg = json.getDouble("rotationDeg").toFloat(),
                rotationSpeedDeg = json.getDouble("rotationSpeedDeg").toFloat(),
                scale = json.getDouble("scale").toFloat(),
                isDragging = false,
                justCaptured = json.getBoolean("justCaptured"),
                movementType = BallMovementType.valueOf(json.getString("movementType")),
                movementPhase = json.getDouble("movementPhase").toFloat(),
                lockedToPattern = json.getBoolean("lockedToPattern"),
                rimBounceCooldownFrames = json.getInt("rimBounceCooldownFrames"),
                rimRejected = json.getBoolean("rimRejected"),
                spawnCode = json.getInt("spawnCode"),
                targetColor = BallColor.valueOf(json.getString("targetColor")),
                colorRevealLocked = json.getBoolean("colorRevealLocked"),
                splitTriggered = json.getBoolean("splitTriggered")
            )
        }
    }

    private fun encodeBasket(basket: Basket): JSONObject {
        return JSONObject()
            .put("color", basket.color.name)
            .put("left", basket.left.toDouble())
            .put("top", basket.top.toDouble())
            .put("right", basket.right.toDouble())
            .put("bottom", basket.bottom.toDouble())
            .put("glowAlpha", basket.glowAlpha.toDouble())
    }

    private fun decodeBaskets(array: JSONArray): List<Basket> {
        return List(array.length()) { index ->
            val json = array.getJSONObject(index)

            Basket(
                color = BallColor.valueOf(json.getString("color")),
                left = json.getDouble("left").toFloat(),
                top = json.getDouble("top").toFloat(),
                right = json.getDouble("right").toFloat(),
                bottom = json.getDouble("bottom").toFloat(),
                glowAlpha = json.getDouble("glowAlpha").toFloat()
            )
        }
    }

    private fun decodeLongSet(array: JSONArray): Set<Long> {
        return List(array.length()) { index ->
            array.getLong(index)
        }.toSet()
    }


    companion object {
        private const val PREF_NAME = "ballrop_game_progress"
        private const val KEY_RESUME_SCENE_SNAPSHOT = "resume_scene_snapshot"
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
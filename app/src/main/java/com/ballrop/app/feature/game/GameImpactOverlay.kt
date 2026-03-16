package com.ballrop.app.feature.game

import com.ballrop.app.core.feedback.GameFeedbackType

data class GameImpactOverlay(
    val type: GameFeedbackType,
    val alpha: Float
)
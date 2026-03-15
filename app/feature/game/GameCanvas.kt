package com.ballrop.app.feature.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ballrop.app.core.design.BallropColors
import com.ballrop.app.model.game.BallColor

@Composable
fun GameCanvas(
    modifier: Modifier = Modifier,
    uiState: GameUiState
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    BallropColors.BackgroundSecondary.copy(alpha = 0.55f),
                    BallropColors.BackgroundPrimary.copy(alpha = 0f)
                )
            ),
            size = Size(canvasWidth, canvasHeight * 0.35f)
        )

        uiState.baskets.forEach { basket ->
            val darkColor = when (basket.color) {
                BallColor.BLUE -> BallropColors.BasketBlueDark
                BallColor.RED -> BallropColors.BasketRedDark
            }

            val mainColor = when (basket.color) {
                BallColor.BLUE -> BallropColors.BasketBlue
                BallColor.RED -> BallropColors.BasketRed
            }

            val basketWidth = basket.right - basket.left
            val basketHeight = basket.bottom - basket.top

            drawRoundRect(
                color = darkColor,
                topLeft = Offset(basket.left, basket.top),
                size = Size(basketWidth, basketHeight),
                cornerRadius = CornerRadius(36f, 36f)
            )

            drawRoundRect(
                color = mainColor,
                topLeft = Offset(basket.left + 10f, basket.top + 10f),
                size = Size(basketWidth - 20f, basketHeight - 20f),
                cornerRadius = CornerRadius(30f, 30f)
            )

            drawRoundRect(
                color = BallropColors.White.copy(alpha = 0.22f),
                topLeft = Offset(basket.left, basket.top),
                size = Size(basketWidth, basketHeight),
                cornerRadius = CornerRadius(36f, 36f),
                style = Stroke(width = 4f)
            )
        }

        val centerX = canvasWidth / 2f
        val basketTop = uiState.baskets.firstOrNull()?.top ?: (canvasHeight * 0.8f)

        drawLine(
            color = BallropColors.White.copy(alpha = 0.06f),
            start = Offset(centerX, canvasHeight * 0.18f),
            end = Offset(centerX, basketTop - 30f),
            strokeWidth = 3f
        )

        uiState.balls.forEach { ball ->
            val ballColor = when (ball.color) {
                BallColor.BLUE -> BallropColors.BallBlue
                BallColor.RED -> BallropColors.BallRed
            }

            drawCircle(
                color = ballColor,
                radius = ball.radius,
                center = Offset(ball.x, ball.y)
            )

            drawCircle(
                color = BallropColors.White.copy(alpha = 0.18f),
                radius = ball.radius,
                center = Offset(ball.x, ball.y),
                style = Stroke(width = 3f)
            )
        }
    }
}
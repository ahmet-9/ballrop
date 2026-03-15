package com.ballrop.app.feature.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.ballrop.app.core.design.BallropColors
import com.ballrop.app.model.game.BallColor
import com.ballrop.app.model.game.BallKind


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
                    BallropColors.BackgroundSecondary.copy(alpha = 1f),
                    BallropColors.BackgroundPrimary.copy(alpha = 1f)
                )
            ),
            size = Size(canvasWidth, canvasHeight)
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    BallropColors.BasketBlue.copy(alpha = 0.08f),
                    BallropColors.Transparent
                )
            ),
            size = Size(canvasWidth, canvasHeight * 0.22f)
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    BallropColors.White.copy(alpha = 0.035f),
                    BallropColors.White.copy(alpha = 0.01f),
                    BallropColors.Transparent
                )
            ),
            size = Size(canvasWidth, canvasHeight * 0.16f)
        )

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    BallropColors.White.copy(alpha = 0.03f),
                    BallropColors.Transparent
                )
            ),
            topLeft = Offset(0f, 0f),
            size = Size(canvasWidth, canvasHeight * 0.26f),
            cornerRadius = CornerRadius(0f, 0f)
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    BallropColors.Transparent,
                    BallropColors.BackgroundSecondary.copy(alpha = 0.14f),
                    BallropColors.BackgroundSecondary.copy(alpha = 0.28f)
                )
            ),
            topLeft = Offset(0f, canvasHeight * 0.50f),
            size = Size(canvasWidth, canvasHeight * 0.50f)
        )

        val basketsTop = uiState.baskets.firstOrNull()?.top ?: (canvasHeight * 0.74f)
        val laneTop = canvasHeight * 0.12f
        val laneBottom = basketsTop - 30f

        drawRoundRect(
            color = BallropColors.White.copy(alpha = 0.02f),
            topLeft = Offset(canvasWidth * 0.06f, laneTop),
            size = Size(canvasWidth * 0.88f, (laneBottom - laneTop).coerceAtLeast(0f)),
            cornerRadius = CornerRadius(36f, 36f)
        )

        drawRoundRect(
            color = BallropColors.White.copy(alpha = 0.035f),
            topLeft = Offset(canvasWidth * 0.06f, laneTop),
            size = Size(canvasWidth * 0.88f, 2f),
            cornerRadius = CornerRadius(50f, 50f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    BallropColors.BasketBlue.copy(alpha = 0.08f),
                    BallropColors.Transparent
                ),
                center = Offset(canvasWidth * 0.18f, canvasHeight * 0.22f),
                radius = canvasWidth * 0.28f
            ),
            radius = canvasWidth * 0.28f,
            center = Offset(canvasWidth * 0.18f, canvasHeight * 0.22f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    BallropColors.BasketRed.copy(alpha = 0.07f),
                    BallropColors.Transparent
                ),
                center = Offset(canvasWidth * 0.82f, canvasHeight * 0.30f),
                radius = canvasWidth * 0.26f
            ),
            radius = canvasWidth * 0.26f,
            center = Offset(canvasWidth * 0.82f, canvasHeight * 0.30f)
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

            val visualLeft = basket.left - (basketWidth * 0.04f)
            val visualTop = basket.top - (basketHeight * 0.10f)
            val visualWidth = basketWidth * 1.08f
            val visualHeight = basketHeight * 1.14f

            if (basket.glowAlpha > 0f) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            mainColor.copy(alpha = 0.30f * basket.glowAlpha),
                            mainColor.copy(alpha = 0.12f * basket.glowAlpha),
                            BallropColors.Transparent
                        )
                    ),
                    topLeft = Offset(visualLeft - 20f, visualTop - 18f),
                    size = Size(visualWidth + 40f, visualHeight + 40f),
                    cornerRadius = CornerRadius(54f, 54f)
                )
            }

            drawRoundRect(
                color = BallropColors.Black.copy(alpha = 0.22f),
                topLeft = Offset(visualLeft, visualTop + 16f),
                size = Size(visualWidth, visualHeight),
                cornerRadius = CornerRadius(42f, 42f)
            )

            drawRoundRect(
                color = darkColor,
                topLeft = Offset(visualLeft, visualTop),
                size = Size(visualWidth, visualHeight),
                cornerRadius = CornerRadius(42f, 42f)
            )

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        mainColor.copy(alpha = 1f),
                        darkColor.copy(alpha = 0.96f)
                    )
                ),
                topLeft = Offset(visualLeft + 8f, visualTop + 8f),
                size = Size(visualWidth - 16f, visualHeight - 16f),
                cornerRadius = CornerRadius(34f, 34f)
            )

            drawRoundRect(
                color = BallropColors.White.copy(alpha = 0.24f),
                topLeft = Offset(visualLeft + 10f, visualTop + 10f),
                size = Size(visualWidth - 20f, 14f),
                cornerRadius = CornerRadius(18f, 18f)
            )

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BallropColors.Black.copy(alpha = 0.14f),
                        BallropColors.Transparent
                    )
                ),
                topLeft = Offset(visualLeft + 12f, visualTop + (visualHeight * 0.36f)),
                size = Size(visualWidth - 24f, visualHeight * 0.40f),
                cornerRadius = CornerRadius(26f, 26f)
            )

            drawRoundRect(
                color = BallropColors.White.copy(alpha = 0.18f + (0.18f * basket.glowAlpha)),
                topLeft = Offset(visualLeft, visualTop),
                size = Size(visualWidth, visualHeight),
                cornerRadius = CornerRadius(42f, 42f),
                style = Stroke(width = 4f)
            )
        }

        uiState.balls.forEach { ball ->
            val baseColor = when (ball.color) {
                BallColor.BLUE -> BallropColors.BallBlue
                BallColor.RED -> BallropColors.BallRed
            }
            val outlineColor = when (ball.kind) {
                BallKind.NORMAL -> BallropColors.White.copy(alpha = 0.22f)
                BallKind.BONUS -> BallropColors.Warning
                BallKind.DANGER -> BallropColors.Error
            }

            val outerGlowColor = when (ball.kind) {
                BallKind.NORMAL -> BallropColors.Transparent
                BallKind.BONUS -> BallropColors.Warning.copy(alpha = 0.22f)
                BallKind.DANGER -> BallropColors.Error.copy(alpha = 0.20f)
            }


            val scaledRadius = ball.radius * ball.scale
            val shadowRadius = if (ball.isDragging) scaledRadius * 1.20f else scaledRadius * 1.10f
            val center = Offset(ball.x, ball.y)

            if (ball.isDragging) {
                drawCircle(
                    color = baseColor.copy(alpha = 0.12f),
                    radius = scaledRadius * 1.45f,
                    center = center
                )
            }
            if (ball.kind != BallKind.NORMAL) {
                drawCircle(
                    color = outerGlowColor,
                    radius = scaledRadius * 1.28f,
                    center = center
                )
            }


            drawCircle(
                color = BallropColors.Black.copy(alpha = 0.20f),
                radius = shadowRadius,
                center = Offset(ball.x, ball.y + 9f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BallropColors.White.copy(alpha = 0.30f),
                        baseColor,
                        baseColor.copy(alpha = 0.96f)
                    ),
                    center = Offset(
                        ball.x - scaledRadius * 0.35f,
                        ball.y - scaledRadius * 0.35f
                    ),
                    radius = scaledRadius * 1.4f
                ),
                radius = scaledRadius,
                center = center
            )

            rotate(
                degrees = ball.rotationDeg,
                pivot = center
            ) {
                drawLine(
                    color = BallropColors.White.copy(alpha = 0.16f),
                    start = Offset(
                        center.x - (scaledRadius * 0.62f),
                        center.y - (scaledRadius * 0.05f)
                    ),
                    end = Offset(
                        center.x + (scaledRadius * 0.62f),
                        center.y + (scaledRadius * 0.05f)
                    ),
                    strokeWidth = scaledRadius * 0.20f,
                    cap = StrokeCap.Round
                )

                drawArc(
                    color = BallropColors.White.copy(alpha = 0.22f),
                    startAngle = 30f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(center.x - scaledRadius, center.y - scaledRadius),
                    size = Size(scaledRadius * 2f, scaledRadius * 2f),
                    style = Stroke(width = 3f)
                )
            }

            drawCircle(
                color = outlineColor,
                radius = scaledRadius,
                center = center,
                style = Stroke(width = 3f)
            )
            when (ball.kind) {
                BallKind.BONUS -> {
                    drawCircle(
                        color = BallropColors.Warning.copy(alpha = 0.18f),
                        radius = scaledRadius * 0.55f,
                        center = center,
                        style = Stroke(width = 4f)
                    )
                }

                BallKind.DANGER -> {
                    drawLine(
                        color = BallropColors.Error.copy(alpha = 0.85f),
                        start = Offset(
                            center.x - scaledRadius * 0.45f,
                            center.y - scaledRadius * 0.45f
                        ),
                        end = Offset(
                            center.x + scaledRadius * 0.45f,
                            center.y + scaledRadius * 0.45f
                        ),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = BallropColors.Error.copy(alpha = 0.85f),
                        start = Offset(
                            center.x + scaledRadius * 0.45f,
                            center.y - scaledRadius * 0.45f
                        ),
                        end = Offset(
                            center.x - scaledRadius * 0.45f,
                            center.y + scaledRadius * 0.45f
                        ),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                }

                BallKind.NORMAL -> Unit
            }


            drawCircle(
                color = BallropColors.White.copy(alpha = 0.34f),
                radius = scaledRadius * 0.24f,
                center = Offset(
                    ball.x - scaledRadius * 0.32f,
                    ball.y - scaledRadius * 0.32f
                )
            )
        }
    }
}

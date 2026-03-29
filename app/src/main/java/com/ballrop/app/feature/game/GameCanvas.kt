package com.ballrop.app.feature.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.ballrop.app.R
import com.ballrop.app.core.design.BallropColors
import com.ballrop.app.model.game.BallColor
import com.ballrop.app.model.game.BallKind

@Composable
fun GameCanvas(
    modifier: Modifier = Modifier,
    uiState: GameUiState
) {
    val redBasketBitmap = ImageBitmap.imageResource(id = R.drawable.basket_red)
    val blueBasketBitmap = ImageBitmap.imageResource(id = R.drawable.basket_blue)

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
            val basketWidth = basket.right - basket.left
            val basketHeight = basket.bottom - basket.top

            val visualLeft = basket.left - (basketWidth * 0.08f)
            val visualTop = basket.top - (basketHeight * 0.22f)


            val aspectRatio = 0.9f

            val visualWidth = basketWidth * 1.20f
            val visualHeight = visualWidth * aspectRatio

            val basketBitmap = when (basket.color) {
                BallColor.BLUE -> blueBasketBitmap
                BallColor.RED -> redBasketBitmap
            }

            if (basket.glowAlpha > 0f) {
                val glowColor = when (basket.color) {
                    BallColor.BLUE -> BallropColors.BasketBlue
                    BallColor.RED -> BallropColors.BasketRed
                }

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.28f * basket.glowAlpha),
                            glowColor.copy(alpha = 0.12f * basket.glowAlpha),
                            BallropColors.Transparent
                        )
                    ),
                    topLeft = Offset(visualLeft - 18f, visualTop - 16f),
                    size = Size(visualWidth + 36f, visualHeight + 36f),
                    cornerRadius = CornerRadius(52f, 52f)
                )
            }

            drawImage(
                image = basketBitmap,
                dstOffset = IntOffset(
                    x = visualLeft.toInt(),
                    y = visualTop.toInt()
                ),
                dstSize = IntSize(
                    width = visualWidth.toInt(),
                    height = visualHeight.toInt()
                ),
                alpha = 1f
            )
        }

        uiState.balls.forEach { ball ->
            val scaledRadius = ball.radius * ball.scale
            val center = Offset(ball.x, ball.y)

            val baseColor = when (ball.kind) {
                BallKind.BONUS -> BallropColors.Warning
                BallKind.DANGER -> BallropColors.Black
                BallKind.NORMAL -> {
                    when (ball.color) {
                        BallColor.BLUE -> BallropColors.BallBlue
                        BallColor.RED -> BallropColors.BallRed
                    }
                }
            }

            val outerGlowColor = when (ball.kind) {
                BallKind.BONUS -> BallropColors.Warning.copy(alpha = 0.26f)
                BallKind.DANGER -> BallropColors.White.copy(alpha = 0.08f)
                BallKind.NORMAL -> {
                    when (ball.color) {
                        BallColor.BLUE -> BallropColors.BallBlue.copy(alpha = 0.18f)
                        BallColor.RED -> BallropColors.BallRed.copy(alpha = 0.18f)
                    }
                }
            }

            val rimColor = when (ball.kind) {
                BallKind.BONUS -> BallropColors.White.copy(alpha = 0.34f)
                BallKind.DANGER -> BallropColors.White.copy(alpha = 0.18f)
                BallKind.NORMAL -> BallropColors.White.copy(alpha = 0.24f)
            }

            // 1) dış aura
            drawCircle(
                color = outerGlowColor,
                radius = scaledRadius * 1.24f,
                center = center
            )

            // 2) alt gölge
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BallropColors.Black.copy(alpha = 0.18f),
                        BallropColors.Transparent
                    ),
                    center = Offset(
                        center.x,
                        center.y + scaledRadius * 0.58f
                    ),
                    radius = scaledRadius * 1.15f
                ),
                radius = scaledRadius * 1.05f,
                center = Offset(center.x, center.y + scaledRadius * 0.16f)
            )

            // 3) ana küre gövdesi
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BallropColors.White.copy(alpha = 0.46f),
                        baseColor.copy(alpha = 0.98f),
                        baseColor.copy(alpha = 0.90f),
                        baseColor.copy(alpha = 1f)
                    ),
                    center = Offset(
                        center.x - scaledRadius * 0.34f,
                        center.y - scaledRadius * 0.38f
                    ),
                    radius = scaledRadius * 1.7f
                ),
                radius = scaledRadius,
                center = center
            )

            // 4) küre alt derinlik katmanı
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BallropColors.Transparent,
                        BallropColors.Black.copy(alpha = 0.10f),
                        BallropColors.Black.copy(alpha = 0.18f)
                    ),
                    center = Offset(
                        center.x + scaledRadius * 0.34f,
                        center.y + scaledRadius * 0.42f
                    ),
                    radius = scaledRadius * 1.25f
                ),
                radius = scaledRadius,
                center = center
            )

            // 5) geniş üst yansıma
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BallropColors.White.copy(alpha = 0.60f),
                        BallropColors.White.copy(alpha = 0.18f),
                        BallropColors.Transparent
                    ),
                    center = Offset(
                        center.x - scaledRadius * 0.42f,
                        center.y - scaledRadius * 0.44f
                    ),
                    radius = scaledRadius * 0.72f
                ),
                radius = scaledRadius * 0.46f,
                center = Offset(
                    center.x - scaledRadius * 0.18f,
                    center.y - scaledRadius * 0.20f
                )
            )

            // 6) keskin parlama noktası
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BallropColors.White.copy(alpha = 0.95f),
                        BallropColors.White.copy(alpha = 0.22f),
                        BallropColors.Transparent
                    ),
                    center = Offset(
                        center.x - scaledRadius * 0.30f,
                        center.y - scaledRadius * 0.34f
                    ),
                    radius = scaledRadius * 0.24f
                ),
                radius = scaledRadius * 0.16f,
                center = Offset(
                    center.x - scaledRadius * 0.22f,
                    center.y - scaledRadius * 0.22f
                )
            )

            // 7) ince cam yansıma yayı
            drawArc(
                color = BallropColors.White.copy(alpha = 0.20f),
                startAngle = 210f,
                sweepAngle = 85f,
                useCenter = false,
                topLeft = Offset(center.x - scaledRadius, center.y - scaledRadius),
                size = Size(scaledRadius * 2f, scaledRadius * 2f),
                style = Stroke(width = scaledRadius * 0.10f)
            )

            // 8) alt iç parlama - gerçek cam hissi
            drawArc(
                color = BallropColors.White.copy(alpha = 0.11f),
                startAngle = 20f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(
                    center.x - scaledRadius * 0.78f,
                    center.y - scaledRadius * 0.70f
                ),
                size = Size(scaledRadius * 1.56f, scaledRadius * 1.42f),
                style = Stroke(width = scaledRadius * 0.08f)
            )

            // 9) dış rim
            drawCircle(
                color = rimColor,
                radius = scaledRadius,
                center = center,
                style = Stroke(width = scaledRadius * 0.09f)
            )

            // 10) bonus top ekstra premium halka
            if (ball.kind == BallKind.BONUS) {
                drawCircle(
                    color = BallropColors.White.copy(alpha = 0.18f),
                    radius = scaledRadius * 0.72f,
                    center = center,
                    style = Stroke(width = scaledRadius * 0.09f)
                )
            }

            // 11) danger top obsidyen hissi
            if (ball.kind == BallKind.DANGER) {
                drawArc(
                    color = BallropColors.White.copy(alpha = 0.16f),
                    startAngle = 230f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - scaledRadius, center.y - scaledRadius),
                    size = Size(scaledRadius * 2f, scaledRadius * 2f),
                    style = Stroke(width = scaledRadius * 0.08f)
                )

                drawLine(
                    color = BallropColors.White.copy(alpha = 0.72f),
                    start = Offset(
                        center.x - scaledRadius * 0.28f,
                        center.y - scaledRadius * 0.28f
                    ),
                    end = Offset(
                        center.x + scaledRadius * 0.28f,
                        center.y + scaledRadius * 0.28f
                    ),
                    strokeWidth = scaledRadius * 0.14f,
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = BallropColors.White.copy(alpha = 0.72f),
                    start = Offset(
                        center.x + scaledRadius * 0.28f,
                        center.y - scaledRadius * 0.28f
                    ),
                    end = Offset(
                        center.x - scaledRadius * 0.28f,
                        center.y + scaledRadius * 0.28f
                    ),
                    strokeWidth = scaledRadius * 0.14f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}


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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.ballrop.app.R
import com.ballrop.app.core.design.BallropColors
import com.ballrop.app.model.game.BallColor
import com.ballrop.app.model.game.BallKind
import android.graphics.Paint
import android.graphics.Typeface

@Composable
fun GameCanvas(
    modifier: Modifier = Modifier,
    uiState: GameUiState
) {
    val redBasketBitmap = ImageBitmap.imageResource(id = R.drawable.basket_red)
    val blueBasketBitmap = ImageBitmap.imageResource(id = R.drawable.basket_blue)
    val greenBasketBitmap = ImageBitmap.imageResource(id = R.drawable.basket_green)

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height



        val basketsTop = uiState.baskets.firstOrNull()?.top ?: (canvasHeight * 0.74f)
        val laneTop = canvasHeight * 0.12f
        val laneBottom = basketsTop - 30f
        val laneHeight = (laneBottom - laneTop).coerceAtLeast(0f)



        // Büyük silik kalan hedef sayısı
        val remainingTarget = uiState.remainingTargetCount.coerceAtLeast(0).toString()
        val bigTextPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(28, 255, 255, 255)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textSize = (canvasWidth * 0.42f).coerceIn(120f, 240f)
        }

        val subTextPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(20, 255, 255, 255)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = (canvasWidth * 0.05f).coerceIn(18f, 28f)
        }

        drawContext.canvas.nativeCanvas.apply {
            val bigX = canvasWidth * 0.5f
            val bigY = canvasHeight * 0.43f

            drawText(
                remainingTarget,
                bigX,
                bigY,
                bigTextPaint
            )

            drawText(
                "KALAN",
                bigX,
                bigY + (canvasWidth * 0.11f),
                subTextPaint
            )
        }

        uiState.baskets.forEach { basket ->
            val basketWidth = basket.right - basket.left
            val basketHeight = basket.bottom - basket.top

            val basketCenterX = (basket.left + basket.right) / 2f

            val aspectRatio = 0.9f
            val visualWidth = basketWidth * 1.18f
            val visualHeight = visualWidth * aspectRatio

            val visualLeft = basketCenterX - (visualWidth / 2f)
            val visualTop = basket.top - (basketHeight * 0.22f)

            val basketBitmap = when (basket.color) {
                BallColor.BLUE -> blueBasketBitmap
                BallColor.RED -> redBasketBitmap
                BallColor.GREEN -> greenBasketBitmap
            }

            if (basket.glowAlpha > 0f) {
                val glowColor = when (basket.color) {
                    BallColor.BLUE -> BallropColors.BasketBlue
                    BallColor.RED -> BallropColors.BasketRed
                    BallColor.GREEN -> BallropColors.BasketGreen
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
                BallKind.BOMB -> BallropColors.Black
                BallKind.ICE -> BallropColors.BallBlue
                BallKind.COLOR_SHIFT,
                BallKind.SPLIT,
                BallKind.NORMAL -> {
                    when (ball.color) {
                        BallColor.BLUE -> BallropColors.BallBlue
                        BallColor.RED -> BallropColors.BallRed
                        BallColor.GREEN -> BallropColors.BallGreen
                    }
                }
            }

            val outerGlowColor = when (ball.kind) {
                BallKind.BONUS -> BallropColors.Warning.copy(alpha = 0.26f)
                BallKind.BOMB -> BallropColors.White.copy(alpha = 0.08f)
                BallKind.ICE -> BallropColors.BallBlue.copy(alpha = 0.24f)
                BallKind.COLOR_SHIFT,
                BallKind.SPLIT,
                BallKind.NORMAL -> {
                    when (ball.color) {
                        BallColor.BLUE -> BallropColors.BallBlue.copy(alpha = 0.18f)
                        BallColor.RED -> BallropColors.BallRed.copy(alpha = 0.18f)
                        BallColor.GREEN -> BallropColors.BallGreen.copy(alpha = 0.18f)
                    }
                }
            }

            val rimColor = when (ball.kind) {
                BallKind.BONUS -> BallropColors.White.copy(alpha = 0.34f)
                BallKind.BOMB -> BallropColors.White.copy(alpha = 0.18f)
                BallKind.ICE -> BallropColors.White.copy(alpha = 0.30f)
                BallKind.COLOR_SHIFT,
                BallKind.SPLIT,
                BallKind.NORMAL -> BallropColors.White.copy(alpha = 0.24f)
            }

            drawCircle(
                color = outerGlowColor,
                radius = scaledRadius * 1.24f,
                center = center
            )

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

            drawArc(
                color = BallropColors.White.copy(alpha = 0.20f),
                startAngle = 210f,
                sweepAngle = 85f,
                useCenter = false,
                topLeft = Offset(center.x - scaledRadius, center.y - scaledRadius),
                size = Size(scaledRadius * 2f, scaledRadius * 2f),
                style = Stroke(width = scaledRadius * 0.10f)
            )

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

            drawCircle(
                color = rimColor,
                radius = scaledRadius,
                center = center,
                style = Stroke(width = scaledRadius * 0.09f)
            )

            if (ball.kind == BallKind.BONUS) {
                drawCircle(
                    color = BallropColors.White.copy(alpha = 0.18f),
                    radius = scaledRadius * 0.72f,
                    center = center,
                    style = Stroke(width = scaledRadius * 0.09f)
                )
            }

            if (ball.kind == BallKind.BOMB) {
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

            if (ball.kind == BallKind.ICE) {
                drawCircle(
                    color = BallropColors.White.copy(alpha = 0.16f),
                    radius = scaledRadius * 0.74f,
                    center = center,
                    style = Stroke(width = scaledRadius * 0.08f)
                )

                drawLine(
                    color = BallropColors.White.copy(alpha = 0.78f),
                    start = Offset(center.x, center.y - scaledRadius * 0.42f),
                    end = Offset(center.x, center.y + scaledRadius * 0.42f),
                    strokeWidth = scaledRadius * 0.08f,
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = BallropColors.White.copy(alpha = 0.78f),
                    start = Offset(center.x - scaledRadius * 0.34f, center.y),
                    end = Offset(center.x + scaledRadius * 0.34f, center.y),
                    strokeWidth = scaledRadius * 0.08f,
                    cap = StrokeCap.Round
                )
            }

            if (ball.kind == BallKind.COLOR_SHIFT) {
                drawArc(
                    color = BallropColors.White.copy(alpha = 0.26f),
                    startAngle = 35f,
                    sweepAngle = 290f,
                    useCenter = false,
                    topLeft = Offset(center.x - scaledRadius * 0.76f, center.y - scaledRadius * 0.76f),
                    size = Size(scaledRadius * 1.52f, scaledRadius * 1.52f),
                    style = Stroke(width = scaledRadius * 0.08f)
                )
            }

            if (ball.kind == BallKind.SPLIT) {
                drawLine(
                    color = BallropColors.White.copy(alpha = 0.78f),
                    start = Offset(center.x, center.y - scaledRadius * 0.34f),
                    end = Offset(center.x, center.y + scaledRadius * 0.34f),
                    strokeWidth = scaledRadius * 0.08f,
                    cap = StrokeCap.Round
                )
            }
        }
    }

}
package com.ballrop.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = BallropColors.BasketBlue,
    onPrimary = BallropColors.White,
    secondary = BallropColors.BasketRed,
    onSecondary = BallropColors.White,
    background = BallropColors.BackgroundPrimary,
    onBackground = BallropColors.TextPrimary,
    surface = BallropColors.Surface,
    onSurface = BallropColors.TextPrimary,
    error = BallropColors.Error,
    onError = BallropColors.White
)

private val LightColors = lightColorScheme(
    primary = BallropColors.BasketBlue,
    onPrimary = BallropColors.White,
    secondary = BallropColors.BasketRed,
    onSecondary = BallropColors.White,
    background = BallropColors.BackgroundPrimary,
    onBackground = BallropColors.TextPrimary,
    surface = BallropColors.Surface,
    onSurface = BallropColors.TextPrimary,
    error = BallropColors.Error,
    onError = BallropColors.White
)

@Composable
fun BallropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = BallropTypography,
        content = content
    )
}
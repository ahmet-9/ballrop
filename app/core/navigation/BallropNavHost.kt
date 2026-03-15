package com.ballrop.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ballrop.app.feature.game.GameScreen
import com.ballrop.app.feature.home.HomeScreen

@Composable
fun BallropNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BallropRoute.Home.route,
        modifier = modifier
    ) {
        composable(BallropRoute.Home.route) {
            HomeScreen(
                onStartClick = {
                    navController.navigate(BallropRoute.Game.route)
                }
            )
        }

        composable(BallropRoute.Game.route) {
            GameScreen()
        }
    }
}
package com.ballrop.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                    navController.navigate(
                        BallropRoute.Game.createRoute(startLevel = 1)
                    )
                },
                onContinueClick = { level ->
                    navController.navigate(
                        BallropRoute.Game.createRoute(startLevel = level)
                    )
                }
            )
        }

        composable(
            route = BallropRoute.Game.route,
            arguments = listOf(
                navArgument("startLevel") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val startLevel = backStackEntry.arguments?.getInt("startLevel") ?: 1

            GameScreen(
                startLevel = startLevel,
                onExitToMenu = {
                    navController.popBackStack()
                }
            )
        }
    }
}
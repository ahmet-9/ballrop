package com.ballrop.app.core.navigation

sealed class BallropRoute(val route: String) {
    data object Home : BallropRoute("home")
    data object Game : BallropRoute("game/{startLevel}") {
        fun createRoute(startLevel: Int): String = "game/$startLevel"
    }
}

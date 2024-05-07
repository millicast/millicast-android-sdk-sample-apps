package io.dolby.app.ui.navigation

sealed class Screen(val route: String) {
    object SubscribeScreen : Screen(route = "subscribeScreen")
    object HomeScreen : Screen(route = "homeScreen")
}

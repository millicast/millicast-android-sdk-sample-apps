package io.dolby.app.navigation

sealed class Screen(val route: String) {
    data object Home : Screen(route = "home")
    data object SubscribeOptions : Screen(route = "subscribeOptions")
    data object Subscribe : Screen(route = "subscribe/{isMultiView}") {
        const val ARG_MULTI_VIEW = "isMultiView"
        fun createRoute(isMultiView: Boolean) = "subscribe/$isMultiView"
    }
    data object Publish : Screen(route = "publish")
}

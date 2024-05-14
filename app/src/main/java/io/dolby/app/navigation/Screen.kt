package io.dolby.app.navigation

import androidx.navigation.NavOptions

sealed class Screen(val route: String, val options: NavOptions? = null) {
    data object Home : Screen(route = "home")
    data object SubscribeOptions : Screen(route = "subscribe")
    data object Subscribe :
        Screen(route = "subscribeOptions/{isMultiView}") {
        const val ARG_MULTI_VIEW = "isMultiView"
        fun createRoute(isMultiView: Boolean) = "subscribeOptions/$isMultiView"
    }
}

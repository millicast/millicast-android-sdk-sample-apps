package io.dolby.app.navigation

import androidx.navigation.NavOptions

sealed class NavigationEvent {
    data class NavigateTo(val route: String, val navOptions: NavOptions? = null) : NavigationEvent()
}

package io.dolby.app.ui.navigation

import androidx.navigation.NavOptions

sealed class NavigationEvent {
    data object NavigateUp : NavigationEvent()
    data class NavigateTo(val route: String, val options: NavOptions? = null) : NavigationEvent()
}

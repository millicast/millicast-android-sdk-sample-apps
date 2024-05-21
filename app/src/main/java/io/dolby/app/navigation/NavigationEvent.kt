package io.dolby.app.navigation

sealed class NavigationEvent {
    data class NavigateTo(val route: String) : NavigationEvent()
}

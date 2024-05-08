package io.dolby.app.ui.navigation

import io.dolby.app.ui.common.ViewAction

interface NavigationContract {
    sealed class NavigationAction(val route: String) : ViewAction {
        data object NavigateToSubscribe : NavigationAction(Screen.SubscribeScreen.route)
        data object NavigateBack : NavigationAction("")
    }
}

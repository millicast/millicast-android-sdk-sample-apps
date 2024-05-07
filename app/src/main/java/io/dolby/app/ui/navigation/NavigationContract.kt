package io.dolby.app.ui.navigation

import androidx.navigation.NavOptions
import io.dolby.app.ui.common.ViewAction
import io.dolby.app.ui.common.ViewSideEffect
import io.dolby.app.ui.common.ViewUIState

interface NavigationContract {
    object NavigationState : ViewUIState

    sealed class NavigationAction(val route: String) : ViewAction {
        data object NavigateToSubscribe : NavigationAction(Screen.SubscribeScreen.route)
        data object NavigateBack : NavigationAction("")
    }

    sealed class NavigationEffect : ViewSideEffect {
        data class NavigateTo(val route: String, val navOptions: NavOptions? = null) : NavigationEffect()
        data object NavigateBack : NavigationEffect()
    }
}

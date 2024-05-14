package io.dolby.app.navigation

import io.dolby.app.common.BaseViewModel
import io.dolby.app.common.ViewSideEffect

class NavigationViewModel(private val navigator: Navigator) :
    BaseViewModel<NavAction, ViewSideEffect>() {
    override fun onUiAction(uiAction: NavAction) {
        when (uiAction) {
            is NavAction.ToSubscribe -> {
                navigator.navigate(NavigationEvent.NavigateTo(Screen.Subscribe.createRoute(uiAction.isMultiView)))
            }

            is NavAction.ToSubscribeOptions -> {
                navigator.navigate(NavigationEvent.NavigateTo(Screen.SubscribeOptions.route))
            }
        }
    }
}

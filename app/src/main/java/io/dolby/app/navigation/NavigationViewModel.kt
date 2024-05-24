package io.dolby.app.navigation

import io.dolby.app.common.BaseViewModel
import io.dolby.app.common.ViewSideEffect

class NavigationViewModel(private val navigator: Navigator) :
    BaseViewModel<NavAction, ViewSideEffect>() {
    override fun onUiAction(uiAction: NavAction) {
        val route = when (uiAction) {
            is NavAction.ToSubscribe -> Screen.Subscribe.createRoute(uiAction.isMultiView)
            NavAction.ToSubscribeOptions -> Screen.SubscribeOptions.route
            NavAction.ToPublish -> Screen.Publish.route
        }
        navigator.navigate(NavigationEvent.NavigateTo(route))
    }
}

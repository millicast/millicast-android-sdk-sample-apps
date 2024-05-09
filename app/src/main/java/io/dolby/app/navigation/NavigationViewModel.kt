package io.dolby.app.navigation

import io.dolby.app.common.StateViewModel
import io.dolby.app.common.ViewSideEffect
import io.dolby.app.common.ViewUIState

class NavigationViewModel(private val navigator: Navigator) :
    StateViewModel<NavAction, ViewUIState, ViewSideEffect>() {
    override fun initializeState() = object : ViewUIState {}
    override fun onUiAction(uiAction: NavAction) {
        when (uiAction) {
            is NavAction.ToSubscribe -> {
                navigator.navigate(Screen.SUBSCRIBE)
            }
        }
    }
}

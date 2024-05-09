package io.dolby.app.navigation

import io.dolby.app.common.StateViewModel
import io.dolby.app.common.ViewSideEffect
import io.dolby.app.common.ViewUIState

class NavigationViewModel(private val navigator: Navigator) :
    StateViewModel<NavigationContract.NavigationAction, ViewUIState, ViewSideEffect>() {
    override fun initializeState() = object : ViewUIState {}
    override fun onUiAction(uiAction: NavigationContract.NavigationAction) {
        when (uiAction) {
            is NavigationContract.NavigationAction.NavigateToSubscribe -> {
                navigator.navigate(uiAction.route)
            }

            is NavigationContract.NavigationAction.NavigateBack ->
                navigator.goBack()
        }
    }
}
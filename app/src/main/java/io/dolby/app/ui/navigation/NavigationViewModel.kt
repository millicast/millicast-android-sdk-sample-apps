package io.dolby.app.ui.navigation

import io.dolby.app.ui.common.StateViewModel

class NavigationViewModel :
    StateViewModel<NavigationContract.NavigationAction, NavigationContract.NavigationState, NavigationContract.NavigationEffect>() {
    override fun initializeState() = NavigationContract.NavigationState

    override fun onUiAction(uiAction: NavigationContract.NavigationAction) {
        when (uiAction) {
            is NavigationContract.NavigationAction.NavigateToSubscribe -> {
                sendEffect(NavigationContract.NavigationEffect.NavigateTo(uiAction.route))
            }

            is NavigationContract.NavigationAction.NavigateBack ->
                sendEffect(NavigationContract.NavigationEffect.NavigateBack)
        }
    }
}

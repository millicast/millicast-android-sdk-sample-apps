package io.dolby.app.navigation

import io.dolby.app.common.ViewAction

sealed class NavAction : ViewAction {
    data object ToSubscribeOptions : NavAction()
    data class ToSubscribe(val isMultiView: Boolean) : NavAction()
    data object ToPublish : NavAction()
}
